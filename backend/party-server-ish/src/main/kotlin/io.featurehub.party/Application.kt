package io.featurehub.party

import cd.connect.app.config.ConfigKey
import cd.connect.app.config.DeclaredConfigResolver
import io.featurehub.edge.EdgeGetFeature
import io.featurehub.health.MetricsHealthRegistration.Companion.registerMetrics
import io.featurehub.jersey.FeatureHubJerseyHost
import io.featurehub.lifecycle.TelemetryFeature
import io.featurehub.mr.ManagementRepositoryFeature
import io.featurehub.mr.dacha2.Dacha2Feature
import io.featurehub.publish.ChannelConstants
import io.featurehub.rest.CacheControlFilter
import io.featurehub.rest.CorsFilter
import io.featurehub.rest.Info
import io.featurehub.web.security.oauth.AuthProviderCollection
import io.featurehub.web.security.oauth.AuthProviders
import io.featurehub.web.security.oauth.NoAuthProviders
import io.featurehub.web.security.oauth.OAuth2Feature
import io.featurehub.web.security.oauth.OAuth2Feature.Companion.oauth2ProvidersExist
import io.featurehub.web.security.saml.SamlEnvironmentalFeature
import io.featurehub.web.security.saml.SamlEnvironmentalFeature.Companion.samlProvidersExist
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Application {
  private val log = LoggerFactory.getLogger(io.featurehub.Application::class.java)

  @ConfigKey("cache.name")
  var name = ChannelConstants.DEFAULT_CACHE_NAME

  init {
    DeclaredConfigResolver.resolve(this)
  }

  @Throws(Exception::class)
  private fun run() {
    log.info("starting party-server-ish, wish me luck!")

    // register our resources, try and tag them as singleton as they are instantiated faster
    val config = ResourceConfig(
      CorsFilter::class.java,
      ManagementRepositoryFeature::class.java,
      EdgeGetFeature::class.java,
      TelemetryFeature::class.java,
      CacheControlFilter::class.java
    )
    
    // check if we should list on a different port
    registerMetrics(config)

    FeatureHubJerseyHost(config).start()
    log.info("Party-Server-ish Launched - (HTTP/2 payloads enabled!)")

    Thread.currentThread().join()
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(Application::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
      System.setProperty("user.timezone", "UTC")
      System.setProperty(Info.APPLICATION_NAME_PROPERTY, "party-server-ish")

      System.clearProperty("nats.urls")

      try {
        Application().run()
      } catch (e: Exception) {
        log.error("failed", e)
        System.exit(-1)
      }
    }
  }
}

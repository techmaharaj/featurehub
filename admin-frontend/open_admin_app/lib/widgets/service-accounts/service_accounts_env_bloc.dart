import 'dart:async';

import 'package:bloc_provider/bloc_provider.dart';
import 'package:mrapi/api.dart';
import 'package:open_admin_app/api/client_api.dart';
import 'package:open_admin_app/api/mr_client_aware.dart';
import 'package:rxdart/rxdart.dart';

class ServiceAccountEnvironments {
  final List<Environment> environments;
  final List<ServiceAccount> serviceAccounts;

  ServiceAccountEnvironments(this.environments, this.serviceAccounts);
}

class ServiceAccountEnvBloc implements Bloc, ManagementRepositoryAwareBloc {
  final ManagementRepositoryClientBloc _mrClient;
  final _serviceAccountEnvironmentsSource = BehaviorSubject<ServiceAccountEnvironments>();
  late ServiceAccountServiceApi _serviceAccountServiceApi;
  late StreamSubscription<List<Environment>> envListener;
  bool firstCall = true;

  ServiceAccountEnvBloc(this._mrClient) {
    _serviceAccountServiceApi = ServiceAccountServiceApi(_mrClient.apiClient);
    envListener = _mrClient.streamValley.currentApplicationEnvironmentsStream
        .listen(_envUpdate);
    // ignore: unawaited_futures
    _mrClient.streamValley.getCurrentApplicationEnvironments();
  }

  void _envUpdate(List<Environment> envs) async {
    if (envs.isEmpty) {
      if (firstCall) {
        firstCall = false;
        // if we aren't an admin, we won't have called this, so lets call it now
        if (!_mrClient.userHasFeaturePermissionsInCurrentApplication) {
          // ignore: unawaited_futures
          _mrClient.streamValley.getCurrentApplicationEnvironments();
        }
      }
      _serviceAccountEnvironmentsSource
          .add(ServiceAccountEnvironments(<Environment>[], <ServiceAccount>[]));
    } else {
      final serviceAccounts = await _serviceAccountServiceApi
          .searchServiceAccountsInPortfolio(_mrClient.currentPortfolio!.id!,
              applicationId: envs[0].applicationId,
              includePermissions: true,
              includeSdkUrls: true)
          .catchError((e, s) {
        _mrClient.dialogError(e, s);
      });

      _serviceAccountEnvironmentsSource
          .add(ServiceAccountEnvironments(envs, serviceAccounts));
    }
  }

  Future<bool> resetApiKey(String id, ResetApiKeyType keyType) {
    return _serviceAccountServiceApi
        .resetApiKey(id, apiKeyType: keyType)
        .then((sa) {
      _envUpdate(_serviceAccountEnvironmentsSource.value.environments);
      return true;
    })
        .catchError((e, s) {
      return false;
    });
  }


  @override
  ManagementRepositoryClientBloc get mrClient => _mrClient;

  Stream<ServiceAccountEnvironments> get serviceAccountEnvironmentsStream =>
      _serviceAccountEnvironmentsSource.stream;


  @override
  void dispose() {
    envListener.cancel();
  }
}

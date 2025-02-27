package io.featurehub.db.model;

import io.ebean.Model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "fh_person_group_link")
public class DbGroupMember extends Model {
  @EmbeddedId
  private final DbGroupMemberKey id;

  @ManyToOne(optional = false)
  @JoinColumn(name="fk_person_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="fk_fh_person_group_link_fh_person"))
  private DbPerson person;

  @ManyToOne(optional = false)
  @JoinColumn(name="fk_group_id", referencedColumnName = "id", foreignKey = @ForeignKey(name=
    "fk_fh_person_group_link_fh_group"))
  private DbGroup group;

  public DbGroupMember(DbGroupMemberKey id) {
    this.id = id;
  }

  public DbGroupMemberKey getId() {
    return id;
  }

  public DbPerson getPerson() {
    return person;
  }

  public DbGroup getGroup() {
    return group;
  }
}


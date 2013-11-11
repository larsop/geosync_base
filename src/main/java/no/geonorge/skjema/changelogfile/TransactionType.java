package no.geonorge.skjema.changelogfile;

import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.UpdateType;

public enum TransactionType {
  INSERT("Insert", InsertType.class),
  DELETE("Delete", DeleteType.class),
  UPDATE("Update", UpdateType.class);
  private String name;
  private Class transactionClass;

  private TransactionType(String name, Class transactionClass) {
    this.name = name;
    this.transactionClass = transactionClass;
  }

  public static TransactionType fromName(String name) {
    return TransactionType.valueOf(name.toUpperCase());
  }

  public String getName() {
    return name;
  }

  public Class getTransactionClass() {
    return transactionClass;
  }
}

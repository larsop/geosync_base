package no.geonorge.skjema.changelogfile;

import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.UpdateType;

public interface ITransactionConsumer {
  public void insert(InsertType o);
  public void update(UpdateType o);
  public void delete(DeleteType o);
}

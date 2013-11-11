package no.geonorge.skjema.changelogfile;

import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.UpdateType;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class TestStaxParsingAndUnmarshal {

  @Test
  public void testStaxParsing() throws XMLStreamException, JAXBException {
    InputStream is = getClass().getClassLoader().getResourceAsStream("tmp/file_xxUD_7_ar5.xml");
    BufferedInputStream bis = new BufferedInputStream(is);

    ChangelogReader changelogReader = new ChangelogReader();
    changelogReader.addTransactionConsumer(new TConsumer());

    // TODO: execute consumer events in a Exceutor for concurrent handling of events


    changelogReader.parse(bis);
  }

  @Ignore
  public class TConsumer implements ITransactionConsumer {
    public TConsumer() {
    }

    @Override
    public void insert(InsertType o) {
      System.err.println("insert" + o);
    }

    @Override
    public void update(UpdateType o) {
      System.err.println("update" + o);
      InsertType it = new UpdateToInsertConverter(o).convert();
    }

    @Override
    public void delete(DeleteType o) {
      System.err.println("delete" + o);
    }
  }
}

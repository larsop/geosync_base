package no.geonorge.skjema.changelogfile;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.UpdateType;
import org.w3c.dom.Node;

import java.lang.reflect.Field;

public class UpdateToInsertConverter {
  private UpdateType o;

  public UpdateToInsertConverter(UpdateType o) {
    this.o = o;
  }

  public InsertType convert()  {
    try {
      if ("ArelaressursFlate".equals(o.getTypeName())) {
        ArealressursFlateType arealressursFlateType = new ArealressursFlateType();
        for (Property p : o.getProperties()) {
          String reference = p.getValueReference().getValue().substring(4);  // assume "app:"...
          ElementNSImpl elementNS = (ElementNSImpl) p.getValue();
          Node valueElement = elementNS.getFirstChild(); // top-level value-element (e.g. område)
          Node valueNode = valueElement.getFirstChild();

          // TODO: valueNode now contains a DOM-structure of the value... is this the intention?

          // TODO: fields can be grabbed this way, but we need to find a proper structure to push on to fields.

          // TODO: how to convert from DOM to a proper structure?

          Field field = ArealressursFlateType.class.getDeclaredField(reference);
          field.setAccessible(true);

        }

      } else {
        // TODO: exception...
      }
    } catch (NoSuchFieldException ignore) {
      // TODO: handle
    }
    return null;

  }
}

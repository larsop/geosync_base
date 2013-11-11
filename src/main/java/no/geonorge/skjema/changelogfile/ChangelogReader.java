package no.geonorge.skjema.changelogfile;

import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.UpdateType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangelogReader {
  private List<ITransactionConsumer> transactionConsumers;

  public ChangelogReader() {
    transactionConsumers = new CopyOnWriteArrayList<>();
  }

  public void addTransactionConsumer(ITransactionConsumer transactionConsumer) {
    transactionConsumers.add(transactionConsumer);
  }

  private Map<TransactionType, JAXBContext> jaxbContext = new HashMap<>();
  private Map<TransactionType, Unmarshaller> unmarshaller = new HashMap<>();

  public void parse(InputStream is) throws XMLStreamException, JAXBException {
    for (TransactionType tc : TransactionType.values()) {
      jaxbContext.put(tc, JAXBContext.newInstance(tc.getTransactionClass()));
      unmarshaller.put(tc, jaxbContext.get(tc).createUnmarshaller());
    }

    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    XMLEventReader r = xmlInputFactory.createXMLEventReader(is);

    r.nextEvent(); // document preamble
    expectStartElement(r.nextTag(), "TransactionCollection", "chlogf", "http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg");
    expectStartElement(r.nextTag(), "transactions", "chlogf");
    eatCharacters(r);
    while (r.hasNext() && r.peek().isStartElement()) {
      TransactionType transactionType = TransactionType.fromName(r.peek().asStartElement().getName().getLocalPart()); // TODO: catch IllegalArgumentException to handle case with non Delete, Insert or Update event
      StartElement se = r.nextEvent().asStartElement();
      StringBuffer transactionString = parseContent(r, se);

      Unmarshaller um = unmarshaller.get(transactionType);

      JAXBElement e = (JAXBElement) um.unmarshal(new StringReader(transactionString.toString()));

      for (ITransactionConsumer tc : transactionConsumers) {
        switch (transactionType) {
          case INSERT:
            tc.insert((InsertType) e.getValue());
            break;
          case DELETE:
            tc.delete((DeleteType) e.getValue());
            break;
          case UPDATE:
            tc.update((UpdateType) e.getValue());
            break;
        }
      }

      eatCharacters(r);
    }
    expectEndElement(r.nextTag(), "transactions");
    expectEndElement(r.nextTag(), "TransactionCollection");

  }

  private void eatCharacters(XMLEventReader r) throws XMLStreamException {
    // eat characters before next element
    if (r.hasNext() && r.peek().isCharacters()) {
      r.nextEvent();
    }

  }

  private StringBuffer parseContent(XMLEventReader r, StartElement se) throws XMLStreamException {
    StringBuffer buf = new StringBuffer();
    printStartElement(buf, se, r, true);
    int level = 1;

    while (level > 0  && r.hasNext()) {
      XMLEvent e = r.nextEvent();
      if (e.isStartElement()) {
        printStartElement(buf, e.asStartElement(), r, false);
        level++;
      } else if (e.isEndElement()) {
        printEndElement(buf, e.asEndElement());
        level--;
      } else if (e.isCharacters()) {
        buf.append(e.asCharacters().getData());
      }

    }
    return buf;
  }


  private void printStartElement(StringBuffer buf, StartElement se, XMLEventReader r, boolean topLevel) throws XMLStreamException {
    QName name = se.getName();
    buf.append("<");
    if (!name.getPrefix().equals("")) {
      buf.append(name.getPrefix()).append(":");
    }
    buf.append(name.getLocalPart());
    for (Iterator i = se.getAttributes(); i.hasNext();) {
      Attribute a = (Attribute) i.next();
      printAttribute(buf, a);
    }
    for (Iterator i = se.getNamespaces(); i.hasNext();) {
      Namespace n = (Namespace) i.next();
      printAttribute(buf, n);
    }
    if (topLevel) {
      buf.append(" xmlns:chlogf=\"http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg\" xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"");
    }
    buf.append(">");
  }

  private void printAttribute(StringBuffer buf, Attribute a) {
    buf.append(" ");
    QName attributeName = a.getName();
    if (!attributeName.getPrefix().equals("")) {
      buf.append(attributeName.getPrefix()).append(":");
    }
    buf.append(attributeName.getLocalPart()).append("=\"").append(a.getValue()).append("\"");
  }

  private void printEndElement(StringBuffer buf, EndElement ee) {
    buf.append("</");
    QName name = ee.getName();
    if (!"".equals(name.getPrefix())) {
      buf.append(name.getPrefix()).append(":");
    }
    buf.append(name.getLocalPart()).append(">");
  }

  private void expectEndElement(XMLEvent event, String name) throws XMLStreamException {
    EndElement ee = event.asEndElement();
    QName qn = ee.getName();
    if (name != null && !name.equals(qn.getLocalPart())) {
      throw new XMLStreamException("Unexpected event: " + event);
    }
  }

  private void expectStartElement(XMLEvent event) throws XMLStreamException {
    expectStartElement(event, null);
  }

  private void expectStartElement(XMLEvent event, String name) throws XMLStreamException {
    expectStartElement(event, name, null, null);
  }

  private void expectStartElement(XMLEvent event, String name, String prefix) throws XMLStreamException {
    expectStartElement(event, name, prefix, null);
  }

  private void expectStartElement(XMLEvent event, String name, String prefix, String namespace) throws XMLStreamException {
    StartElement se = event.asStartElement();
    QName qn = se.getName();
    if (((name != null) && !name.equals(qn.getLocalPart())) ||
        ((prefix != null) && !prefix.equals(qn.getPrefix())) ||
        ((namespace != null) && !namespace.equals(namespace))) {
      throw new XMLStreamException("Unexpected event: " + event);
    }
  }
}

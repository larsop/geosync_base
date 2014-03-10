package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file

 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

// 
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.skogoglandskap.ar5.SimpleAr5Transformerer1;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;
import no.skogoglandskap.util.BuildTopo;
import no.skogoglandskap.util.PolygonFeature;
import no.skogoglandskap.util.TopoGeometry;
import opengis.net.gml_3_2_1.gml.AbstractCurveSegmentType;
import opengis.net.gml_3_2_1.gml.AbstractCurveType;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractSurfacePatchType;
import opengis.net.gml_3_2_1.gml.AbstractSurfaceType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.OrientableCurveType;
import opengis.net.gml_3_2_1.gml.PolygonPatchType;
import opengis.net.gml_3_2_1.gml.RingType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePatchArrayPropertyType;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateCompleteChangelogFile {
	private Logger logger = Logger.getLogger(TestGenerateCompleteChangelogFile.class);

	@Autowired
	private ChangeLogMarshallerHelper changelogfileJaxb2Helper;
}

package no.skogoglandskap.datamodel.postgres.provider;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Polygon;

/**
 * TODO Move this code to a non public repo
 * 
 * This is where we find the provider data.
 * 
 * AR5 is a national (for Norway) land capability classification system and map
 * dataset that describes land resources, with emphasis on capability for
 * agriculture and natural plant production. The dataset is primarily intended
 * for land use planning, public management, agriculture and forestry. The
 * result of the classification is a discrete polygon coverage where each
 * polygon feature has a set of attribute values. The primary level of
 * classification is surface type (arealtype) based on criteria for vegetation,
 * cultivation and drainage. Second level attributes are forest site quality
 * class (skogbonitet), forest cover type (treslag) and soil conditions
 * (grunnforhold). In general the minimum mapping unit is 0.2 ha. The initial
 * version of the AR5 dataset is derived from an existing map dataset (DMK) that
 * covers the productive part of Norway. AR5 shall be regularly updated along
 * with other detailed datasets.
 * 
 * {@link http://www.skogoglandskap.no/filearchive/netthb_0106.pdf}
 * 
 * 
 * @author Lars Opsahl
 * 
 */
@Entity
@Table(name = "ar5_flate", schema = "sde_ar5_prov")
@SequenceGenerator(name = "ID_SEQ", sequenceName = "ar5.ar5_flate_id_seq")
@Proxy(lazy = false)
public class Ar5FlateProvSimpleFeatureEntity implements java.io.Serializable,
		no.skogoglandskap.util.PolygonFeature {

	private static final long serialVersionUID = 1L;

	/**
	 * Id generated by database
	 */

	@Id
	@Column(name = "sl_sdeid", unique = true, nullable = false)
	private Integer sl_sdeid;

	// surface type
	@Column()
	private Byte artype;

	// forest site quality class
	@Column()
	private Byte arskogbon;

	// forest cover type
	@Column()
	private Byte artreslag;

	@Column()
	private Byte argrunnf;

	// area in square meter
	@Column()
	private Float areal;

	@Column()
	private Byte maalemetode;

	@Column()
	private Byte synbarhet;

	@Column()
	private Date verifiseringsdato;

	@Column()
	private Date datafangstdato;

	@Column()
	private String kartid;

	// the border of this area type
	@Column(name = "geo")
	@Type(type = "org.hibernatespatial.GeometryUserType")
	private Polygon geo;

	@Column()
	private String arkartstd;

	@Column()
	private String opphav;

	public Ar5FlateProvSimpleFeatureEntity() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MarkslagFlateEntity [areal=" + areal + ", argrunnf=" + argrunnf
				+ ", arkartstd=" + arkartstd + ", arskogbon=" + arskogbon
				+ ", artreslag=" + artreslag + ", artype=" + artype
				+ ", datafangstdato=" + datafangstdato + ", kartid=" + kartid
				+ " , maalemetode=" + maalemetode + ", opphav=" + opphav
				+ ", sl_sdeid=" + getId() + ", synbarhet=" + synbarhet
				+ ", verifiseringsdato=" + verifiseringsdato + "]";
	}

	public Byte getArtype() {
		return this.artype;
	}

	public void setArtype(Byte artype) {
		this.artype = artype;
	}

	public Byte getArskogbon() {
		return this.arskogbon;
	}

	public void setArskogbon(Byte arskogbon) {
		this.arskogbon = arskogbon;
	}

	public Byte getArtreslag() {
		return this.artreslag;
	}

	public void setArtreslag(Byte artreslag) {
		this.artreslag = artreslag;
	}

	public Byte getArgrunnf() {
		return this.argrunnf;
	}

	public void setArgrunnf(Byte argrunnf) {
		this.argrunnf = argrunnf;
	}

	public Float getAreal() {
		// work around to round down value
		return this.areal;
	}

	public void setAreal(Float areal) {
		if (areal != null) {
			int decimalPlace = 5;
			BigDecimal bd = new BigDecimal(areal);
			bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
			this.areal = bd.floatValue();
		} else {
			this.areal = areal;
		}
	}

	public Byte getMaalemetode() {
		return this.maalemetode;
	}

	public void setMaalemetode(Byte maalemetode) {
		this.maalemetode = maalemetode;
	}

	public Byte getSynbarhet() {
		return this.synbarhet;
	}

	public void setSynbarhet(Byte synbarhet) {
		this.synbarhet = synbarhet;
	}

	public Date getVerifiseringsdato() {
		return this.verifiseringsdato;
	}

	public void setVerifiseringsdato(Date verifiseringsdato) {
		this.verifiseringsdato = verifiseringsdato;
	}

	public Date getDatafangstdato() {
		return this.datafangstdato;
	}

	public void setDatafangstdato(Date datafangstdato) {
		this.datafangstdato = datafangstdato;
	}

	public String getKartid() {
		return this.kartid;
	}

	public void setKartid(String kartid) {
		this.kartid = kartid;
	}

	public Polygon getGeo() {
		return this.geo;
	}

	public void setGeo(Polygon geo) {
		this.geo = geo;
	}

	public String getArkartstd() {
		return this.arkartstd;
	}

	public void setArkartstd(String arkartstd) {
		this.arkartstd = arkartstd;
	}

	public String getOpphav() {
		return this.opphav;
	}

	public void setOpphav(String opphav) {
		this.opphav = opphav;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Ar5FlateProvSimpleFeatureEntity)) {
			return false;
		}

		Ar5FlateProvSimpleFeatureEntity rhs = (Ar5FlateProvSimpleFeatureEntity) o;
		return new EqualsBuilder()
				// .append(sl_sdeid, rhs.sl_sdeid)
				.append(artype, rhs.artype)
				.append(arskogbon, rhs.arskogbon)
				.append(artreslag, rhs.artreslag)
				.append(argrunnf, rhs.argrunnf)
				.append(areal, rhs.areal)
				.append(maalemetode, rhs.maalemetode)
				.append(synbarhet, rhs.synbarhet)
				.append(verifiseringsdato, rhs.verifiseringsdato)
				.append(datafangstdato, rhs.datafangstdato)
				.append(kartid, rhs.kartid)
				.append(((geo == null && rhs.geo == null) || geo
						.equalsExact(rhs.geo)),
						true).append(arkartstd, rhs.arkartstd)
				.append(opphav, rhs.opphav).isEquals();
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return sl_sdeid;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.sl_sdeid = id;
	}

}

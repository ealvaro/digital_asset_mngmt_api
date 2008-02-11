package net.bzresults.astmgr.dao;

import java.util.Date;
import java.util.List;

import net.bzresults.astmgr.model.DAMAsset;
import net.bzresults.astmgr.model.DAMTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Data access object (DAO) for domain model class DAMTag.
 * 
 * @see net.bzresults.astmgr.model.DAMTag
 * @author escobara
 */

public class TagDAO extends HibernateDaoSupport {
	private static final Log log = LogFactory.getLog(TagDAO.class);
	// property constants
	public static final String TAG_ATTRIB = "tagAttrib";
	public static final String TAG_VALUE = "tagValue";

	protected void initDao() {
		// do nothing
	}

	public void save(DAMTag transientInstance) {
		log.warn("saving DAMTag instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.warn("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(DAMTag persistentInstance) {
		log.warn("deleting DAMTag instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.warn("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DAMTag findById(java.lang.Long id) {
		log.warn("getting DAMTag instance with id: " + id);
		try {
			DAMTag instance = (DAMTag) getHibernateTemplate().get("net.bzresults.astmgr.model.DAMTag", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(DAMTag instance) {
		log.warn("finding DAMTag instance by example");
		try {
			List results = getHibernateTemplate().findByExample(instance);
			log.warn("find by example successful, result size: " + results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.warn("finding DAMTag instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMTag as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List<DAMTag> getTagsByAttribValue(String attribName, Object attribValue) {
		log.warn("finding Asset Tags instances with attribute: " + attribName + ", attribute value: " + attribValue);
		try {
			String queryString = "from DAMTag as model where model." + TAG_ATTRIB + " = '" + attribName
					+ "' AND model." + TAG_VALUE + "= '" + attribValue + "'";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find by property " + attribName + " failed", re);
			throw re;
		}
	}

	public List findByTagAttrib(Object tagAttrib) {
		return findByProperty(TAG_ATTRIB, tagAttrib);
	}

	public List findByTagValue(Object tagValue) {
		return findByProperty(TAG_VALUE, tagValue);
	}

	public List findByAssetId(Long assetId) {
		log.warn("finding DAMTag instance for DAMAsset id = " + assetId);
		try {
			String queryString = "from DAMTag as model where model.assetId.id = ?";
			return getHibernateTemplate().find(queryString, assetId);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findAll() {
		log.warn("finding all DAMTag instances");
		try {
			String queryString = "from DAMTag";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public DAMTag merge(DAMTag detachedInstance) {
		log.warn("merging DAMTag instance");
		try {
			DAMTag result = (DAMTag) getHibernateTemplate().merge(detachedInstance);
			log.warn("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(DAMTag instance) {
		log.warn("attaching dirty DAMTag instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DAMTag instance) {
		log.warn("attaching clean DAMTag instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static TagDAO getFromApplicationContext(ApplicationContext ctx) {
		return (TagDAO) ctx.getBean("TagDAO");
	}
}
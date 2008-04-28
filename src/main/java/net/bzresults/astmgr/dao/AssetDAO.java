package net.bzresults.astmgr.dao;

import java.util.Date;
import java.util.List;

import net.bzresults.astmgr.model.DAMAsset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Data access object (DAO) for domain model class DAMAsset.
 * 
 * @see net.bzresults.astmgr.model.DAMAsset
 * @author escobara
 */

public class AssetDAO extends HibernateDaoSupport {
	private static final Log log = LogFactory.getLog(AssetDAO.class);
	// property constants
	public static final String FILE_NAME = "fileName";
	public static final String VALVE_ID = "valveId";
	public static final String UPLOAD_DATE = "uploadDate";
	public static final String CLIENT_ID = "clientId";
	public static final String READ_ONLY = "readOnly";
	public static final String OWNER_ID = "ownerId";
	public static final Long RECENT_CRITERIA = 60 * 60 * 1000L; // 1 Hr. in milliseconds.
	private static final String[] CRITERIA_PARAMS = { AssetDAO.CLIENT_ID, AssetDAO.VALVE_ID };

	protected void initDao() {
		// do nothing
	}

	public void save(DAMAsset transientInstance) {
		log.debug("saving DAMAsset instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.warn("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(DAMAsset persistentInstance) {
		log.debug("deleting DAMAsset instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.warn("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DAMAsset findById(java.lang.Long id) {
		log.debug("getting DAMAsset instance with id: " + id);
		try {
			DAMAsset instance = (DAMAsset) getHibernateTemplate().get("net.bzresults.astmgr.model.DAMAsset", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(DAMAsset instance) {
		log.debug("finding DAMAsset instance by example");
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
		log.debug("finding DAMAsset instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMAsset as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List<DAMAsset> getRecentItems(Object[] values, Date createDate) {
		log.debug("finding recently uploaded Assets with timestamp : " + createDate);
		Date beforeDate = new Date();
		beforeDate.setTime(createDate.getTime() - RECENT_CRITERIA);
		log.debug("recent criteria used: upload_date >= '" + beforeDate.getTime() + "'");
		try {
			String queryString = "from DAMAsset as model where model." + CRITERIA_PARAMS[0] + " = " + values[0]
					+ " AND (model." + CRITERIA_PARAMS[1] + "= '" + values[1] + "' OR model." + CRITERIA_PARAMS[1]
					+ "= '" + DAMAsset.ALL_VALVES + "') AND model." + AssetDAO.UPLOAD_DATE + ">= '"
					+ beforeDate.getTime() + "'";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find recently created assets failed", re);
			throw re;
		}
	}

	public List findByFileName(String fileName) {
		// return findByProperty(FILE_NAME, fileName);
		log.debug("finding DAMAsset instance with property name: like " + fileName);
		try {
			String queryString = "from DAMAsset as model where model." + FILE_NAME + " like '%" + fileName + "%'";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}

	}

	public List findByValveId(Object valveId) {
		return findByProperty(VALVE_ID, valveId);
	}

	public List findByUploadDate(Object uploadDate) {
		return findByProperty(UPLOAD_DATE, uploadDate);
	}

	public List findByClientId(Object clientId) {
		return findByProperty(CLIENT_ID, clientId);
	}

	public List findByReadOnly(Object readOnly) {
		return findByProperty(READ_ONLY, readOnly);
	}

	public List findByOwnerId(Object ownerId) {
		return findByProperty(OWNER_ID, ownerId);
	}

	public List findAll() {
		log.debug("finding all DAMAsset instances");
		try {
			String queryString = "from DAMAsset";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public DAMAsset merge(DAMAsset detachedInstance) {
		log.debug("merging DAMAsset instance");
		try {
			DAMAsset result = (DAMAsset) getHibernateTemplate().merge(detachedInstance);
			log.warn("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(DAMAsset instance) {
		log.debug("attaching dirty DAMAsset instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DAMAsset instance) {
		log.debug("attaching clean DAMAsset instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static AssetDAO getFromApplicationContext(ApplicationContext ctx) {
		return (AssetDAO) ctx.getBean("AssetDAO");
	}
}
package net.bzresults.astmgr.dao;

import java.util.Date;
import java.util.List;

import net.bzresults.astmgr.model.DAMFolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Data access object (DAO) for domain model class DAMFolder.
 * 
 * @see net.bzresults.astmgr.model.DAMFolder
 * @author escobara
 */

public class FolderDAO extends HibernateDaoSupport {
	private static final Log log = LogFactory.getLog(FolderDAO.class);
	// property constants
	public static final String ID = "id";
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String VALVE_ID = "valveId";
	public static final String HIDDEN = "hidden";
	public static final String READ_ONLY = "readOnly";
	public static final String CLIENT_ID = "clientId";
	public static final String FORMAT = "format";
	public static final String SYSTEM = "system";
	public static final String CREATE_DATE = "createDate";
	public static final String PATH = "path";
	private static final String[] CRITERIA_PARAMS = { FolderDAO.CLIENT_ID, FolderDAO.VALVE_ID };

	protected void initDao() {
		// do nothing
	}

	public void save(DAMFolder transientInstance) {
		log.debug("saving DAMFolder instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(DAMFolder persistentInstance) {
		log.debug("deleting DAMFolder instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DAMFolder findById(java.lang.Long id) {
		log.debug("getting DAMFolder instance with id: " + id);
		try {
			DAMFolder instance = (DAMFolder) getHibernateTemplate().get("net.bzresults.astmgr.model.DAMFolder", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(DAMFolder instance) {
		log.debug("finding DAMFolder instance by example");
		try {
			List results = getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, result size: " + results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	/**
	 * @param propertyName
	 * @param value
	 * @return
	 */
	// TODO this method is not DAM safe to call unless clientid and valveid are included in the query.
	public List findByProperty(String propertyName, Object value) {
		log.debug("finding DAMFolder instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMFolder as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public DAMFolder getValveFolder(Object[] values, String valveid) {
		return getFolder(values, valveid);
	}

	public DAMFolder getRoot(Object[] values) {
		return getFolder(values, DAMFolder.ROOTNAME);
	}

	private DAMFolder getFolder(Object[] values, String folderName) {
		log.debug("finding Folder instance with  " + FolderDAO.NAME + "=" + folderName);
		if (folderName.equals(DAMFolder.ROOTNAME))
			values[1] = DAMFolder.ALL_VALVES;
		try {
			String queryString = "from DAMFolder as model where " + "model." + CRITERIA_PARAMS[0] + " = " + values[0]
					+ " AND model." + CRITERIA_PARAMS[1] + "= '" + values[1] + "' AND model." + FolderDAO.NAME + "='"
					+ folderName + "'";
			// If a unique one is needed call the other getFolder function with id parameter.
			// I know there is a find() method with value[] as parameter but this way was faster
			return (DAMFolder) getHibernateTemplate().find(queryString).get(0);
		} catch (IndexOutOfBoundsException ioobe) {
			log.error("find folder: " + CRITERIA_PARAMS[0] + "='" + values[0] + "' & " + FolderDAO.NAME + "='"
					+ folderName + "' failed");
			return null;
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			return null;
		}
	}

	public List findByDescription(Object description) {
		return findByProperty(DESCRIPTION, description);
	}

	public List findByName(Object name) {
		return findByProperty(NAME, name);
	}

	public List findByValveId(Object valveId) {
		return findByProperty(VALVE_ID, valveId);
	}

	public List findByHidden(Object hidden) {
		return findByProperty(HIDDEN, hidden);
	}

	public List findByReadOnly(Object readOnly) {
		return findByProperty(READ_ONLY, readOnly);
	}

	public List findByClientId(Object clientId) {
		return findByProperty(CLIENT_ID, clientId);
	}

	public List findByFormat(Object format) {
		return findByProperty(FORMAT, format);
	}

	public List findBySystem(Object system) {
		return findByProperty(SYSTEM, system);
	}

	public List findByCreateDate(Object createDate) {
		return findByProperty(CREATE_DATE, createDate);
	}

	public List findByPath(Object path) {
		return findByProperty(PATH, path);
	}

	public List findAll() {
		log.debug("finding all DAMFolder instances");
		try {
			String queryString = "from DAMFolder";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public DAMFolder merge(DAMFolder detachedInstance) {
		log.debug("merging DAMFolder instance");
		try {
			DAMFolder result = (DAMFolder) getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(DAMFolder instance) {
		log.debug("attaching dirty DAMFolder instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
		}
	}

	public void update(DAMFolder instance) {
		log.debug("updating DAMFolder instance");
		try {
			getHibernateTemplate().update(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void persist(DAMFolder instance) {
		log.debug("updating DAMFolder instance");
		try {
			getHibernateTemplate().persist(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DAMFolder instance) {
		log.debug("attaching clean DAMFolder instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static FolderDAO getFromApplicationContext(ApplicationContext ctx) {
		return (FolderDAO) ctx.getBean("FolderDAO");
	}
}
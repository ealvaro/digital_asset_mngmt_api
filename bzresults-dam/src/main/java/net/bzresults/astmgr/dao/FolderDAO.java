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
	public static final String ROOTNAME = "ROOT";

	protected void initDao() {
		// do nothing
	}

	public void save(DAMFolder transientInstance) {
		log.warn("saving DAMFolder instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.warn("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(DAMFolder persistentInstance) {
		log.warn("deleting DAMFolder instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.warn("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DAMFolder findById(java.lang.Long id) {
		log.warn("getting DAMFolder instance with id: " + id);
		try {
			DAMFolder instance = (DAMFolder) getHibernateTemplate().get("net.bzresults.astmgr.model.DAMFolder", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(DAMFolder instance) {
		log.warn("finding DAMFolder instance by example");
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
		log.warn("finding DAMFolder instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMFolder as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public DAMFolder getRoot(String propertyName, Object value) {
		return getFolder(propertyName, value, FolderDAO.ROOTNAME);
	}

	public DAMFolder getFolder(String propertyName, Object value, java.lang.Long id) {
		log.warn("finding Folder with id = " + id + " instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMFolder as model where model." + propertyName + "= ? AND model."
					+ FolderDAO.ID + "='" + id + "'";
			// There should only be one folder with that id.
			return (DAMFolder) getHibernateTemplate().find(queryString, value).get(0);
		} catch (IndexOutOfBoundsException ioobe) {
			log.error("find folder: " + propertyName + "='" + value + "' & " + FolderDAO.ID + "='" + id + "' failed");
			return null;
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			return null;
		}
	}

	public DAMFolder getFolder(String propertyName, Object value, String folderName) {
		log.warn("finding " + folderName + " Folder instance with property: " + propertyName + ", value: " + value);
		try {
			String queryString = "from DAMFolder as model where model." + propertyName + "= ? AND model."
					+ FolderDAO.NAME + "='" + folderName + "'";
			// TODO handle situation when there is more than one folder with
			// same name at different hierarchy levels (return the correct one).
			// There should only be one folder with that name, and if there is
			// more than one the first one will be returned.
			// If a unique one is needed call the other getFolder function with
			// id parameter.
			return (DAMFolder) getHibernateTemplate().find(queryString, value).get(0);
		} catch (IndexOutOfBoundsException ioobe) {
			log.error("find folder: " + propertyName + "='" + value + "' & " + FolderDAO.NAME + "='" + folderName
					+ "' failed");
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
		log.warn("finding all DAMFolder instances");
		try {
			String queryString = "from DAMFolder";
			return getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public DAMFolder merge(DAMFolder detachedInstance) {
		log.warn("merging DAMFolder instance");
		try {
			DAMFolder result = (DAMFolder) getHibernateTemplate().merge(detachedInstance);
			log.warn("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(DAMFolder instance) {
		log.warn("attaching dirty DAMFolder instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DAMFolder instance) {
		log.warn("attaching clean DAMFolder instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.warn("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static FolderDAO getFromApplicationContext(ApplicationContext ctx) {
		return (FolderDAO) ctx.getBean("FolderDAO");
	}
}
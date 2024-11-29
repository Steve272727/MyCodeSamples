	import org.apache.commons.lang3.StringUtils;
	import org.hibernate.Criteria;
	import org.hibernate.Hibernate;
	import org.hibernate.Query;
	import org.hibernate.Session;
	import org.hibernate.criterion.Criterion;
	import org.hibernate.criterion.DetachedCriteria;
	import org.hibernate.criterion.Order;
	import org.hibernate.criterion.ProjectionList;
	import org.hibernate.criterion.Projections;
	import org.hibernate.criterion.Property;
	import org.hibernate.criterion.Restrictions;
	import org.hibernate.criterion.Subqueries;
	import org.hibernate.sql.JoinType;

	private Criteria getRecords(SystemUser user) {
		Date now = new Date();
		Date currentDate = DateUtil.roundBackCalendar(now).getTime();

		DetachedCriteria expiredRecs = DetachedCriteria.forClass(
				Expired.class, "exp");
		expiredRecs.add(Restrictions.eq("exp.type",
				RecordStatusType.STATUS1.getDescription()));
		expiredRecs.add(Restrictions.eqProperty("exp.personRecord.key", "pc.key"));
		expiredRecs.setProjection(Property.forName("exp.personRecord.key"));
		expiredRecs.add(Restrictions.or(Restrictions.isNull("exp.endDate"),
				Restrictions.ge("exp.endDate", currentDate)));

		Criteria criteria = getCurrentSession().createCriteria(
				PersonRecord.class, "pc");
		criteria.add(Restrictions.eq("statusType", Long.valueOf(
				RecordStatusType.STATUS2.getKey())));

		DetachedCriteria userRegistrationCriteria = DetachedCriteria
				.forClass(UserRegistration.class);
		userRegistrationCriteria.add(Restrictions.isNotNull("startDate"));
		userRegistrationCriteria.setProjection(Property
				.forName("personRecord.key"));
		criteria.add(Subqueries.propertyNotIn("key", expiredRecs));
		criteria.add(Subqueries.propertyNotIn("key", userRegistrationCriteria));

		if (user.isAdmin()) {
			DetachedCriteria sharedOrganisation = DetachedCriteria
					.forClass(SharedOrganisation.class);
			sharedOrganisation.add(Restrictions.isNull("endDate"));
			sharedOrganisation.add(Restrictions.eq("organisation.key", user
					.getJurisdiction().getKey()));
			sharedOrganisation
					.setProjection(Property.forName("personRecord.key"));

			criteria.add(Restrictions
					.disjunction()
					.add(Restrictions.eq("owningOrganisation.key", user
							.getJurisdiction().getKey()))
					.add(Subqueries.propertyIn("key", sharedOrganisation)));
		} else if (user.isManager()) {
			DetachedCriteria manager1Crteria = DetachedCriteria
					.forClass(AssignedLocalManager.class);
			manager1Crteria.add(Restrictions.isNull("endDate")).add(
					Restrictions.eq("localManager", user.getUserId()));
			manager1Crteria.setProjection(Property.forName("personRecord.key"));

			DetachedCriteria manager2Criteria = DetachedCriteria
					.forClass(AssignedCaseManager.class);
			manager2Criteria.add(Restrictions.isNull("endDate")).add(
					Restrictions.eq("caseManager", user.getUserId()));
			manager2Criteria.setProjection(Property.forName("personRecord.key"));

			criteria.add(Restrictions.disjunction()
					.add(Subqueries.propertyIn("key", manager1Crteria))
					.add(Subqueries.propertyIn("key", manager2Criteria)));
		}

		return criteria;
	}

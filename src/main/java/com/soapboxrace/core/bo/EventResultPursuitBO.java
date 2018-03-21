package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.PursuitArbitrationPacket;
import com.soapboxrace.jaxb.http.PursuitEventResult;

@Stateless
public class EventResultPursuitBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private SocialBO socialBo;

	@EJB
	private RewardPursuitBO rewardPursuitBO;

	@EJB
	private LegitRaceBO legitRaceBO;

	@EJB
	private CarDamageBO carDamageBO;

	public PursuitEventResult handlePursitEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, PursuitArbitrationPacket pursuitArbitrationPacket,
			Boolean isBusted) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);

		boolean legit = legitRaceBO.isLegit(activePersonaId, pursuitArbitrationPacket, eventSessionEntity);

		if (pursuitArbitrationPacket.getHacksDetected() > 0) {
			socialBo.sendReport(0L, activePersonaId, 3, "Server sent a report for cheat", (int) pursuitArbitrationPacket.getCarId(), 0,
					pursuitArbitrationPacket.getHacksDetected());
		}

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		eventDataEntity.setAlternateEventDurationInMilliseconds(pursuitArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setCarId(pursuitArbitrationPacket.getCarId());
		eventDataEntity.setCopsDeployed(pursuitArbitrationPacket.getCopsDeployed());
		eventDataEntity.setCopsDisabled(pursuitArbitrationPacket.getCopsDisabled());
		eventDataEntity.setCopsRammed(pursuitArbitrationPacket.getCopsRammed());
		eventDataEntity.setCostToState(pursuitArbitrationPacket.getCostToState());
		eventDataEntity.setEventDurationInMilliseconds(pursuitArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(pursuitArbitrationPacket.getFinishReason());
		eventDataEntity.setHacksDetected(pursuitArbitrationPacket.getHacksDetected());
		eventDataEntity.setHeat(pursuitArbitrationPacket.getHeat());
		eventDataEntity.setInfractions(pursuitArbitrationPacket.getInfractions());
		eventDataEntity.setLongestJumpDurationInMilliseconds(pursuitArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRoadBlocksDodged(pursuitArbitrationPacket.getRoadBlocksDodged());
		eventDataEntity.setSpikeStripsDodged(pursuitArbitrationPacket.getSpikeStripsDodged());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(pursuitArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(pursuitArbitrationPacket.getTopSpeed());
		eventDataDao.update(eventDataEntity);

		PursuitEventResult pursuitEventResult = new PursuitEventResult();
		pursuitEventResult.setAccolades(legit ? rewardPursuitBO.getPursuitAccolades(activePersonaId, pursuitArbitrationPacket, isBusted) : new Accolades());
		pursuitEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, pursuitArbitrationPacket.getCarId(), 0,
				pursuitArbitrationPacket.getEventDurationInMilliseconds()));
		pursuitEventResult.setEventId(eventDataEntity.getEvent().getId());
		pursuitEventResult.setEventSessionId(eventSessionId);
		pursuitEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		pursuitEventResult.setHeat(1);
		pursuitEventResult.setInviteLifetimeInMilliseconds(0);
		pursuitEventResult.setLobbyInviteId(0);
		pursuitEventResult.setPersonaId(activePersonaId);
		return pursuitEventResult;
	}

}
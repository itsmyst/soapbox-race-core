package com.soapboxrace.core.bo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.soapboxrace.core.dao.EventDataDAO;
import com.soapboxrace.core.dao.EventSessionDAO;
import com.soapboxrace.core.jpa.EventDataEntity;
import com.soapboxrace.core.jpa.EventSessionEntity;
import com.soapboxrace.core.xmpp.OpenFireSoapBoxCli;
import com.soapboxrace.core.xmpp.XmppEvent;
import com.soapboxrace.jaxb.http.Accolades;
import com.soapboxrace.jaxb.http.ArrayOfRouteEntrantResult;
import com.soapboxrace.jaxb.http.ExitPath;
import com.soapboxrace.jaxb.http.RouteArbitrationPacket;
import com.soapboxrace.jaxb.http.RouteEntrantResult;
import com.soapboxrace.jaxb.http.RouteEventResult;
import com.soapboxrace.jaxb.xmpp.XMPP_ResponseTypeRouteEntrantResult;
import com.soapboxrace.jaxb.xmpp.XMPP_RouteEntrantResultType;

@Stateless
public class EventResultRouteBO {

	@EJB
	private EventSessionDAO eventSessionDao;

	@EJB
	private EventDataDAO eventDataDao;

	@EJB
	private SocialBO socialBo;

	@EJB
	private OpenFireSoapBoxCli openFireSoapBoxCli;

	@EJB
	private RewardRouteBO rewardRouteBO;

	@EJB
	private LegitRaceBO legitRaceBO;

	@EJB
	private CarDamageBO carDamageBO;

	public RouteEventResult handleRaceEnd(EventSessionEntity eventSessionEntity, Long activePersonaId, RouteArbitrationPacket routeArbitrationPacket) {
		Long eventSessionId = eventSessionEntity.getId();
		eventSessionEntity.setEnded(System.currentTimeMillis());

		eventSessionDao.update(eventSessionEntity);

		boolean legit = legitRaceBO.isLegit(activePersonaId, routeArbitrationPacket, eventSessionEntity);

		if (routeArbitrationPacket.getHacksDetected() > 0) {
			socialBo.sendReport(0L, activePersonaId, 3, "Server sent a report for cheat", (int) routeArbitrationPacket.getCarId(), 0,
					routeArbitrationPacket.getHacksDetected());
		}

		XMPP_RouteEntrantResultType xmppRouteResult = new XMPP_RouteEntrantResultType();
		xmppRouteResult.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		xmppRouteResult.setEventDurationInMilliseconds(routeArbitrationPacket.getEventDurationInMilliseconds());
		xmppRouteResult.setEventSessionId(eventSessionId);
		xmppRouteResult.setFinishReason(routeArbitrationPacket.getFinishReason());
		xmppRouteResult.setPersonaId(activePersonaId);
		xmppRouteResult.setRanking(routeArbitrationPacket.getRank());
		xmppRouteResult.setTopSpeed(routeArbitrationPacket.getTopSpeed());

		XMPP_ResponseTypeRouteEntrantResult routeEntrantResultResponse = new XMPP_ResponseTypeRouteEntrantResult();
		routeEntrantResultResponse.setRouteEntrantResult(xmppRouteResult);

		EventDataEntity eventDataEntity = eventDataDao.findByPersonaAndEventSessionId(activePersonaId, eventSessionId);
		eventDataEntity.setAlternateEventDurationInMilliseconds(routeArbitrationPacket.getAlternateEventDurationInMilliseconds());
		eventDataEntity.setBestLapDurationInMilliseconds(routeArbitrationPacket.getBestLapDurationInMilliseconds());
		eventDataEntity.setCarId(routeArbitrationPacket.getCarId());
		eventDataEntity.setEventDurationInMilliseconds(routeArbitrationPacket.getEventDurationInMilliseconds());
		eventDataEntity.setEventModeId(eventDataEntity.getEvent().getEventModeId());
		eventDataEntity.setFinishReason(routeArbitrationPacket.getFinishReason());
		eventDataEntity.setFractionCompleted(routeArbitrationPacket.getFractionCompleted());
		eventDataEntity.setHacksDetected(routeArbitrationPacket.getHacksDetected());
		eventDataEntity.setLongestJumpDurationInMilliseconds(routeArbitrationPacket.getLongestJumpDurationInMilliseconds());
		eventDataEntity.setNumberOfCollisions(routeArbitrationPacket.getNumberOfCollisions());
		eventDataEntity.setPerfectStart(routeArbitrationPacket.getPerfectStart());
		eventDataEntity.setPersonaId(activePersonaId);
		eventDataEntity.setRank(routeArbitrationPacket.getRank());
		eventDataEntity.setSumOfJumpsDurationInMilliseconds(routeArbitrationPacket.getSumOfJumpsDurationInMilliseconds());
		eventDataEntity.setTopSpeed(routeArbitrationPacket.getTopSpeed());
		eventDataDao.update(eventDataEntity);

		ArrayOfRouteEntrantResult arrayOfRouteEntrantResult = new ArrayOfRouteEntrantResult();
		for (EventDataEntity racer : eventDataDao.getRacers(eventSessionId)) {
			RouteEntrantResult routeEntrantResult = new RouteEntrantResult();
			routeEntrantResult.setBestLapDurationInMilliseconds(racer.getBestLapDurationInMilliseconds());
			routeEntrantResult.setEventDurationInMilliseconds(racer.getEventDurationInMilliseconds());
			routeEntrantResult.setEventSessionId(eventSessionId);
			routeEntrantResult.setFinishReason(racer.getFinishReason());
			routeEntrantResult.setPersonaId(racer.getPersonaId());
			routeEntrantResult.setRanking(racer.getRank());
			routeEntrantResult.setTopSpeed(racer.getTopSpeed());
			arrayOfRouteEntrantResult.getRouteEntrantResult().add(routeEntrantResult);

			if (!racer.getPersonaId().equals(activePersonaId)) {
				XmppEvent xmppEvent = new XmppEvent(racer.getPersonaId(), openFireSoapBoxCli);
				xmppEvent.sendRaceEnd(routeEntrantResultResponse);
				if (routeArbitrationPacket.getRank() == 1) {
					xmppEvent.sendEventTimingOut(eventSessionId);
				}
			}
		}

		RouteEventResult routeEventResult = new RouteEventResult();
		routeEventResult.setAccolades(legit ? rewardRouteBO.getRouteAccolades(activePersonaId, routeArbitrationPacket) : new Accolades());
		routeEventResult.setDurability(carDamageBO.updateDamageCar(activePersonaId, routeArbitrationPacket.getCarId(),
				routeArbitrationPacket.getNumberOfCollisions(), routeArbitrationPacket.getEventDurationInMilliseconds()));
		routeEventResult.setEntrants(arrayOfRouteEntrantResult);
		routeEventResult.setEventId(eventDataEntity.getEvent().getId());
		routeEventResult.setEventSessionId(eventSessionId);
		routeEventResult.setExitPath(ExitPath.EXIT_TO_FREEROAM);
		routeEventResult.setInviteLifetimeInMilliseconds(0);
		routeEventResult.setLobbyInviteId(0);
		routeEventResult.setPersonaId(activePersonaId);
		return routeEventResult;
	}

}
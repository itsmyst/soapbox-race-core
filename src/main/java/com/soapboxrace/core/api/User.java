package com.soapboxrace.core.api;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.soapboxrace.core.api.util.Secured;
import com.soapboxrace.core.bo.InviteTicketBO;
import com.soapboxrace.core.bo.TokenSessionBO;
import com.soapboxrace.core.bo.UserBO;
import com.soapboxrace.jaxb.http.UserInfo;
import com.soapboxrace.jaxb.login.LoginStatusVO;

@Path("User")
public class User {

	@EJB
	private UserBO userBO;

	@EJB
	private TokenSessionBO tokenBO;

	@EJB
	private InviteTicketBO inviteTicketBO;

	@POST
	@Secured
	@Path("GetPermanentSession")
	@Produces(MediaType.APPLICATION_XML)
	public UserInfo getPermanentSession(@HeaderParam("userId") Long userId) {
		tokenBO.deleteByUserId(userId);
		String randomUUID = tokenBO.createToken(userId);
		UserInfo userInfo = userBO.getUserById(userId);
		userInfo.getUser().setSecurityToken(randomUUID);
		userBO.createXmppUser(userInfo);
		return userInfo;
	}

	@POST
	@Secured
	@Path("SecureLoginPersona")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLoginPersona(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId, @QueryParam("personaId") Long personaId) {
		tokenBO.setActivePersonaId(securityToken, personaId, false);
		userBO.secureLoginPersona(userId, personaId);
		return "";
	}

	@POST
	@Secured
	@Path("SecureLogoutPersona")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLogoutPersona(@HeaderParam("securityToken") String securityToken, @HeaderParam("userId") Long userId, @QueryParam("personaId") Long personaId) {
		tokenBO.setActivePersonaId(securityToken, 0L, true);
		return "";
	}

	@POST
	@Secured
	@Path("SecureLogout")
	@Produces(MediaType.APPLICATION_XML)
	public String secureLogout() {
		return "";
	}

	@GET
	@Path("authenticateUser")
	@Produces(MediaType.APPLICATION_XML)
	public Response authenticateUser(@QueryParam("email") String email, @QueryParam("password") String password) {
		LoginStatusVO loginStatusVO = tokenBO.login(email, password);
		if (loginStatusVO.isLoginOk()) {
			return Response.ok(loginStatusVO).build();
		}
		return Response.serverError().entity(loginStatusVO).build();
	}

	@GET
	@Path("createUser")
	@Produces(MediaType.APPLICATION_XML)
	public Response createUser(@QueryParam("email") String email, @QueryParam("password") String password, @QueryParam("inviteTicket") String inviteTicket) {
		LoginStatusVO loginStatusVO = userBO.createUserWithTicket(email, password, inviteTicket);
		if (loginStatusVO != null && loginStatusVO.isLoginOk()) {
			loginStatusVO = tokenBO.login(email, password);
			return Response.ok(loginStatusVO).build();
		}
		return Response.serverError().entity(loginStatusVO).build();
	}

}

package foo;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.Collections;

import foo.PetitionEndpoint;

@WebServlet("/connection")
public class Connection extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
        String token = request.getParameter("token");
		HttpSession session = request.getSession();
		Object sessionId = session.getAttribute("userId");
		String statusMessage = "{\"session\": false}";
		if(sessionId != null) {
			String sessionString = session.getAttribute("userId").toString();
			statusMessage = "{\"session\": true, \"userId\": \""+sessionString+"\"}";
		} 
		else if (token !=null){
			GooglePublicKeysManager manager = new GooglePublicKeysManager(new NetHttpTransport(), new GsonFactory());

			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(manager)
					.setAudience(Collections.singletonList("909783088248-9rg5s8fdfkn402gkkiugmmbc3pjnm2e5.apps.googleusercontent.com"))
					.build();
					
			String credentials = token;
			try {
				GoogleIdToken idToken = verifier.verify(credentials);
				if (idToken != null) {
					Payload payload = idToken.getPayload();

					String userId = payload.getSubject();
					String name = (String) payload.get("name");

					PetitionEndpoint user = new PetitionEndpoint();
					user.addUser(userId, name);
					session.setAttribute("userId", userId);
					statusMessage = "{\"session\": true, \"userId\": \""+userId+"\"}";
				}
				else {
					System.out.println("token invalide");
				}	
			} catch (Exception e) {
			}
		}
		response.getWriter().write(statusMessage);
		return;
    }

}
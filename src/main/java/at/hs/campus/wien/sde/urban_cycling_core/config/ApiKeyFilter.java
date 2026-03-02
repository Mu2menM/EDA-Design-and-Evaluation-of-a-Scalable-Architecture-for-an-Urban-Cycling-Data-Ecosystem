package at.hs.campus.wien.sde.urban_cycling_core.config;

import java.io.IOException;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter implements Filter {
  private static final String API_KEY = "thesis-secret-key"; // Should be replaced for production

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String key = req.getHeader("X-API-KEY");

    if (API_KEY.equals(key)) {
      chain.doFilter(request, response);
    } else {
      ((HttpServletResponse) response).setStatus(401);
      response.getWriter().write("Invalid API Key");
    }
  }
}

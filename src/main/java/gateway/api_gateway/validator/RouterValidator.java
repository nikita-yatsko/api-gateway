package gateway.api_gateway.validator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.function.Predicate;
import java.util.List;

@Component
public class RouterValidator {

    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh"
    );

    public final Predicate<ServerHttpRequest> isSecured = request ->
            OPEN_PATHS.stream()
                    .noneMatch(openPath ->
                            PathPatternParser.defaultInstance
                                    .parse(openPath)
                                    .matches(request.getPath().pathWithinApplication()));
}

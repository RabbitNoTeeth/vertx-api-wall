package fun.bookish.vertx.api.wall.core;

import fun.bookish.vertx.api.wall.config.ApiAccessOptions;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface ApiAccessHandler extends Handler<RoutingContext> {

    static ApiAccessHandler create(ApiAccessOptions apiAccessOptions){
        return new ApiAccessHandlerImpl(apiAccessOptions);
    }

}

package code.mentor.config;

import code.mentor.component.OdataJpaServiceFactory;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.core.rest.ODataRootLocator;
import org.apache.olingo.odata2.core.rest.app.ODataApplication;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ApplicationPath("/odata")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(OdataJpaServiceFactory serviceFactory, EntityManagerFactory entityManagerFactory) {
        ODataApplication oDataApplication = new ODataApplication();
        oDataApplication
                .getClasses()
                .forEach( c -> {
                    if ( !ODataRootLocator.class.isAssignableFrom(c)) {
                        register(c);
                    }
                });
        register(new ODataServiceRootLocator(serviceFactory));
        register(new EntityManagerFilter(entityManagerFactory));
    }

    @Path("/")
    public static class ODataServiceRootLocator extends ODataRootLocator {

        private OdataJpaServiceFactory serviceFactory;

        @Inject
        public ODataServiceRootLocator (OdataJpaServiceFactory serviceFactory) {
            this.serviceFactory = serviceFactory;
        }

        @Override
        public ODataServiceFactory getServiceFactory() {
            return this.serviceFactory;
        }
    }

    @Provider
    public static class EntityManagerFilter implements ContainerRequestFilter,
            ContainerResponseFilter {
        public static final String EM_REQUEST_ATTRIBUTE =
                EntityManagerFilter.class.getName() + "_ENTITY_MANAGER";
        private final EntityManagerFactory entityManagerFactory;

        @Context
        private HttpServletRequest httpRequest;
        public EntityManagerFilter(EntityManagerFactory entityManagerFactory) {
            this.entityManagerFactory = entityManagerFactory;
        }

        @Override
        public void filter(ContainerRequestContext containerRequestContext) throws IOException {
            EntityManager entityManager = this.entityManagerFactory.createEntityManager();
            httpRequest.setAttribute(EM_REQUEST_ATTRIBUTE, entityManager);
            if (!"GET".equalsIgnoreCase(containerRequestContext.getMethod())) {
                entityManager.getTransaction().begin();
            }
        }
        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            EntityManager entityManager = (EntityManager) httpRequest.getAttribute(EM_REQUEST_ATTRIBUTE);
            if (!"GET".equalsIgnoreCase(requestContext.getMethod())) {
                EntityTransaction entityTransaction = entityManager.getTransaction(); //we do not commit because it's just a READ
                if (entityTransaction.isActive() && !entityTransaction.getRollbackOnly()) {
                    entityTransaction.commit();
                }
            }
            entityManager.close();
        }
    }

}

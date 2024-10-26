package code.mentor.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;
import org.apache.olingo.odata2.jpa.processor.ref.util.CustomODataJPAProcessor;
import org.glassfish.jersey.client.JerseyClient;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;

public class CustomODataServiceFactory extends ODataServiceFactory {
    private ODataJPAContext oDataJPAContext;
    private ODataContext oDataContext;


    @Override
    public ODataService createService(final ODataContext context) throws ODataException {
        oDataContext = context;
        oDataJPAContext = initializeODataJPAContext();

        validatePreConditions();

        ODataJPAFactory factory = ODataJPAFactory.createFactory();
        ODataJPAAccessFactory accessFactory = factory.getODataJPAAccessFactory();

        if (oDataJPAContext.getODataContext() == null) {
            oDataJPAContext.setODataContext(context);
        }

        ODataSingleProcessor oDataSingleProcessor = new CustomODataJPAProcessor(oDataJPAContext);

        EdmProvider edmProvider = accessFactory.createJPAEdmProvider(oDataJPAContext);
        return createODataSingleProcessorService(edmProvider, oDataSingleProcessor);
    }

    private void validatePreConditions() throws ODataJPAException {
        if (oDataJPAContext.getEntityManagerFactory() == null) {
            throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.ENTITY_MANAGER_NOT_INITIALIZED, null);
        }
    }

    public final ODataJPAContext getODataJPAContext() throws ODataJPAException {
        if (oDataJPAContext == null) {
            oDataJPAContext = ODataJPAFactory.createFactory().getODataJPAAccessFactory().createODataJPAContext();
        }
        if (oDataContext != null) {
            oDataJPAContext.setODataContext(oDataContext);
        }
        return oDataJPAContext;
    }

    protected ODataJPAContext initializeODataJPAContext() throws ODataJPAException {
        ODataJPAContext oDataJPAContext = this.getODataJPAContext();
        ODataContext oDataContext = oDataJPAContext.getODataContext();

        HttpServletRequest request = (HttpServletRequest) oDataContext.getParameter(
                ODataContext.HTTP_SERVLET_REQUEST_OBJECT);
        EntityManager entityManager = (EntityManager) request.getAttribute("EntityManagerFilter.EM_REQUEST_ATTRIBUTE");
        oDataJPAContext.setEntityManager((javax.persistence.EntityManager) entityManager);


        oDataJPAContext.setPersistenceUnitName("default");
        oDataJPAContext.setContainerManaged(true);
        return oDataJPAContext;
    }
}

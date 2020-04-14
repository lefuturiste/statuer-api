package fr.lefuturiste.statuer.stores;

import fr.lefuturiste.statuer.models.Namespace;
import fr.lefuturiste.statuer.models.Project;
import fr.lefuturiste.statuer.models.Service;

public class QueryStore {

    public static class ObjectQueryResult {
        public Namespace namespace = null;
        public Project project = null;
        public Service service = null;
        public String namespaceSlug = null;
        public String projectSlug = null;
        public String serviceSlug = null;
    }

    public static ObjectQueryResult getObjectsFromQuery(String path) {
        String[] pathComponents = path.split("\\.");
        if (pathComponents.length == 0 || path.length() == 0) {
            return null;
        }
        ObjectQueryResult objectQueryResult = new ObjectQueryResult();
        Namespace namespace = NamespaceStore.getOneBySlug(pathComponents[0]);
        objectQueryResult.namespaceSlug = pathComponents[0];
        if (pathComponents.length >= 2) {
            objectQueryResult.projectSlug = pathComponents[1];
        }
        if (pathComponents.length >= 3) {
            objectQueryResult.serviceSlug = pathComponents[2];
        }
        if (namespace != null) {
            objectQueryResult.namespace = namespace;
            if (pathComponents.length >= 2) {
                Project project = ProjectStore.getOneBySlugAndByNamespace(pathComponents[1], namespace);
                objectQueryResult.project = project;
                if (project != null) {
                    if (pathComponents.length >= 3) {
                        objectQueryResult.service = ServiceStore.getOneBySlugAndByProject(pathComponents[2], project);
                    }
                }
            }
        }
        return objectQueryResult;
    }
}

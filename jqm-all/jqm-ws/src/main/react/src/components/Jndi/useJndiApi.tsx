import { useCallback, useState } from "react";
import { JndiResource } from "./JndiResource";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const apiUrl = "/admin/jndi";

export const useJndiApi = () => {
    const { displayError, displaySuccess } = useNotificationService();
    const [resources, setResources] = useState<JndiResource[]>([]);

    const fetchResources = useCallback(async () => {
        return APIService.get(apiUrl)
            .then((resources) => setResources(resources))
            .catch(displayError);
    }, [displayError]);

    const saveResource = useCallback(
        async (resource: JndiResource) => {
            return APIService.post(apiUrl, resource)
                .then(() => {
                    fetchResources();
                    displaySuccess(
                        `Successfully saved resource: ${resource.name}`
                    );
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess]
    );
    const deleteResource = useCallback(
        async (resourceIds: number[]) => {
            return await Promise.all(
                resourceIds.map((id) => APIService.delete(`${apiUrl}/${id}`))
            )
                .then(() => {
                    fetchResources();
                    displaySuccess(
                        `Successfully deleted resource${resourceIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess]
    );

    return {
        resources,
        fetchResources,
        saveResource,
        deleteResource,
    };
};

export default useJndiApi;

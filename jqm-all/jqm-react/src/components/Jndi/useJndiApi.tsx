import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { JndiResource } from "./JndiResource";

const apiUrl = "/jndi";

export const useJndiApi = () => {
    const { displayError, displaySuccess } = useNotificationService();
    const [resources, setResources] = useState<JndiResource[]>([]);

    const fetchResources = useCallback(async () => {
        return APIService.get(apiUrl)
            .then((resources) => setResources(resources))
            .catch(displayError);
    }, [displayError]);

    const createResource = useCallback(
        async (newResource: JndiResource) => {
            return APIService.post(apiUrl, newResource)
                .then(() => {
                    fetchResources();
                    displaySuccess(
                        `Successfully created new resource: ${newResource.name}`
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
                        `Successfully deleted resource${
                            resourceIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess]
    );
    const updateResource = useCallback(
        async (resource: JndiResource) => {
            return APIService.put(`${apiUrl}/${resource.id}`, resource)
                .then(() => {
                    fetchResources();
                    displaySuccess("Successfully saved resource");
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess]
    );

    return {
        resources,
        fetchResources,
        createResource,
        updateResource,
        deleteResource,
    };
};

export default useJndiApi;

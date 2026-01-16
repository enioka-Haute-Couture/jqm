import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { JndiResource } from "./JndiResource";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const apiUrl = "/admin/jndi";

export const useJndiApi = () => {
    const { t } = useTranslation();
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
                        t("jndi.messages.successSave", { name: resource.name })
                    );
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess, t]
    );
    const deleteResource = useCallback(
        async (resourceIds: number[]) => {
            return await Promise.all(
                resourceIds.map((id) => APIService.delete(`${apiUrl}/${id}`))
            )
                .then(() => {
                    fetchResources();
                    displaySuccess(
                        t("jndi.messages.successDelete", {
                            count: resourceIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchResources, displayError, displaySuccess, t]
    );

    return {
        resources,
        fetchResources,
        saveResource,
        deleteResource,
    };
};

export default useJndiApi;

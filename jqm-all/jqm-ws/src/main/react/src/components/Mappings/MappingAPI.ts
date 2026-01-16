import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { Mapping } from "./Mapping";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/qmapping";

export const useMappingAPI = () => {
    const { t } = useTranslation();
    const { displayError, displaySuccess } = useNotificationService();

    const [mappings, setMappings] = useState<Mapping[] | null>();

    const fetchMappings = useCallback(async () => {
        return APIService.get(API_URL)
            .then((mappings) => setMappings(mappings))
            .catch(displayError);
    }, [displayError]);

    const createMapping = useCallback(
        async (newMapping: Mapping) => {
            return APIService.post(API_URL, newMapping)
                .then(() => {
                    fetchMappings();
                    displaySuccess(
                        t("mappings.messages.successCreate", {
                            nodeName: newMapping.nodeName,
                            queueName: newMapping.queueName,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess, t]
    );

    const deleteMappings = useCallback(
        async (mappingIds: number[]) => {
            return await Promise.all(
                mappingIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchMappings();
                    displaySuccess(
                        t("mappings.messages.successDelete", {
                            count: mappingIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess, t]
    );

    const updateMapping = useCallback(
        async (mapping: Mapping) => {
            return APIService.put(`${API_URL}/${mapping.id}`, mapping)
                .then(() => {
                    fetchMappings();
                    displaySuccess(t("mappings.messages.successSave"));
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess, t]
    );
    return {
        mappings,
        fetchMappings,
        createMapping,
        updateMapping,
        deleteMappings,
    };
};

export default useMappingAPI;

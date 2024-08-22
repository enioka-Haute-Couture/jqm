import { useCallback, useState } from "react";
import { Mapping } from "./Mapping";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/qmapping";

export const useMappingAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const [mappings, setMappings] = useState<Mapping[] | null>();

    const fetchMappings = useCallback(async () => {
        return APIService.get(API_URL)
            .then((mappings) => setMappings(mappings))
            .catch(displayError);
    }, [displayError]);

    const createMapping = useCallback(
        // TODO: handle 400 when https://github.com/enioka/jqm/issues/446 is done
        async (newMapping: Mapping) => {
            return APIService.post(API_URL, newMapping)
                .then(() => {
                    fetchMappings();
                    displaySuccess(
                        `Successfully created mapping between ${newMapping.nodeName} and ${newMapping.queueName}`
                    );
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess]
    );

    const deleteMappings = useCallback(
        async (mappingIds: number[]) => {
            return await Promise.all(
                mappingIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchMappings();
                    displaySuccess(
                        `Successfully deleted mapping${
                            mappingIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess]
    );

    const updateMapping = useCallback(
        // TODO: handle 400 when https://github.com/enioka/jqm/issues/446 is done
        async (mapping: Mapping) => {
            return APIService.put(`${API_URL}/${mapping.id}`, mapping)
                .then(() => {
                    fetchMappings();
                    displaySuccess("Successfully saved mapping");
                })
                .catch(displayError);
        },
        [fetchMappings, displayError, displaySuccess]
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

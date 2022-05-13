import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Parameter } from "./Parameter";

const apiUrl = "/admin/prm";

export const useParametersApi = () => {
    const { displayError, displaySuccess } = useNotificationService();
    const [parameters, setParameters] = useState<Parameter[] | null>();

    const fetchParameters = useCallback(async () => {
        return APIService.get(apiUrl)
            .then((parameters) => setParameters(parameters))
            .catch(displayError);
    }, [displayError]);

    const createParameter = useCallback(
        async (newParam: Parameter) => {
            return APIService.post(apiUrl, newParam)
                .then(() => {
                    fetchParameters();
                    displaySuccess(
                        `Successfully created new parameter: ${newParam.key}`
                    );
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess]
    );

    const deleteParameter = useCallback(
        async (paramIds: number[]) => {
            return await Promise.all(
                paramIds.map((id) => APIService.delete(`${apiUrl}/${id}`))
            )
                .then(() => {
                    fetchParameters();
                    displaySuccess(
                        `Successfully deleted parameter${paramIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess]
    );

    const updateParameter = useCallback(
        async (parameter: Parameter) => {
            return APIService.put(`${apiUrl}/${parameter.id}`, parameter)
                .then(() => {
                    fetchParameters();
                    displaySuccess("Successfully saved parameter");
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess]
    );

    return {
        parameters,
        fetchParameters,
        createParameter,
        updateParameter,
        deleteParameter,
    };
};

export default useParametersApi;

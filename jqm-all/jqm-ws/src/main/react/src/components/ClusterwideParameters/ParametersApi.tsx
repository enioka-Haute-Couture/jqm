import { useCallback, useState } from "react";
import { Parameter } from "./Parameter";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { useTranslation } from "react-i18next";

const apiUrl = "/admin/prm";

export const useParametersApi = () => {
    const { t } = useTranslation();
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
                        t("clusterParameters.messages.successCreate", { key: newParam.key })
                    );
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess, t]
    );

    const deleteParameter = useCallback(
        async (paramIds: number[]) => {
            return await Promise.all(
                paramIds.map((id) => APIService.delete(`${apiUrl}/${id}`))
            )
                .then(() => {
                    fetchParameters();
                    displaySuccess(
                        t("clusterParameters.messages.successDelete", { count: paramIds.length })
                    );
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess, t]
    );

    const updateParameter = useCallback(
        async (parameter: Parameter) => {
            return APIService.put(`${apiUrl}/${parameter.id}`, parameter)
                .then(() => {
                    fetchParameters();
                    displaySuccess(t("clusterParameters.messages.successSave"));
                })
                .catch(displayError);
        },
        [fetchParameters, displayError, displaySuccess, t]
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

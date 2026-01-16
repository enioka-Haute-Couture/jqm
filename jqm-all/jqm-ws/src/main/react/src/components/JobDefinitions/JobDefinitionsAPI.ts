import { useCallback, useState } from "react";
import { JobDefinition, JobType } from "./JobDefinition";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { useTranslation } from "react-i18next";

function pathTypeToJobType(pathType: string): JobType {
    switch (pathType) {
        case "DIRECTEXECUTABLE":
            return JobType.process;
        case "POWERSHELLCOMMAND":
        case "DEFAULTSHELLCOMMAND":
            return JobType.shell;
        default:
            return JobType.java;
    }
}

function jobDefinitionToPayload(jobDefinition: JobDefinition): any {
    let payload: any = jobDefinition;

    payload = {
        ...payload,
        application: jobDefinition.tags.application,
        module: jobDefinition.tags.module,
        keyword1: jobDefinition.tags.keyword1,
        keyword2: jobDefinition.tags.keyword2,
        keyword3: jobDefinition.tags.keyword3,
        javaClassName: jobDefinition.properties.javaClassName
            ? jobDefinition.properties.javaClassName
            : "com.company.product.ClassName",
        jarPath: jobDefinition.properties.jarPath,
        pathType: jobDefinition.properties.pathType,
        classLoaderId: jobDefinition.properties.classLoaderId,
    };
    delete payload.tags;
    delete payload.properties;

    return payload;
}

const API_URL = "/admin/jd";

export const useJobDefinitionsAPI = () => {
    const { t } = useTranslation();
    const { displayError, displaySuccess } = useNotificationService();

    const [jobDefinitions, setJobDefinitions] = useState<
        JobDefinition[] | null
    >();

    const fetchJobDefinitions = useCallback(async () => {
        return APIService.get(API_URL)
            .then((response) =>
                setJobDefinitions(
                    response.map((jobDefinition: any) => {
                        return {
                            ...jobDefinition,
                            tags: {
                                application: jobDefinition.application,
                                module: jobDefinition.module,
                                keyword1: jobDefinition.keyword1,
                                keyword2: jobDefinition.keyword2,
                                keyword3: jobDefinition.keyword3,
                            },
                            jobType: pathTypeToJobType(jobDefinition.pathType),
                            properties: {
                                javaClassName: jobDefinition.javaClassName,
                                jarPath: jobDefinition.jarPath,
                                jobType: pathTypeToJobType(
                                    jobDefinition.pathType
                                ),
                                pathType: jobDefinition.pathType,
                                classLoaderId: jobDefinition.classLoaderId,
                            },
                        };
                    })
                )
            )
            .catch(displayError);
    }, [displayError]);

    const createJobDefinition = useCallback(
        async (newJobDefinition: JobDefinition) => {
            return APIService.post(
                API_URL,
                jobDefinitionToPayload(newJobDefinition)
            )
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess(
                        t("jobDefinitions.messages.successCreate", {
                            name: newJobDefinition.applicationName,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess, t]
    );

    const deleteJobDefinitions = useCallback(
        async (jobDefinitionsIds: number[]) => {
            return await Promise.all(
                jobDefinitionsIds.map((id) =>
                    APIService.delete(`${API_URL}/${id}`)
                )
            )
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess(
                        t("jobDefinitions.messages.successDelete", {
                            count: jobDefinitionsIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess, t]
    );

    const updateJobDefinition = useCallback(
        async (jobDefinition: JobDefinition) => {
            return APIService.put(
                `${API_URL}/${jobDefinition.id}`,
                jobDefinitionToPayload(jobDefinition)
            )
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess(t("jobDefinitions.messages.successSave"));
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess, t]
    );
    return {
        jobDefinitions,
        fetchJobDefinitions,
        createJobDefinition,
        updateJobDefinition,
        deleteJobDefinitions,
    };
};

export default useJobDefinitionsAPI;

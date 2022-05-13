import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { JobDefinition, JobType } from "./JobDefinition";

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
        javaClassName: jobDefinition.properties.javaClassName ? jobDefinition.properties.javaClassName : "com.company.product.ClassName",
        jarPath: jobDefinition.properties.jarPath,
        pathType: jobDefinition.properties.pathType,
    }
    delete payload.tags;
    delete payload.properties;

    return payload;
}

const API_URL = "/admin/jd";

export const useJobDefinitionsAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const [jobDefinitions, setJobDefinitions] = useState<
        JobDefinition[] | null
    >();

    const fetchJobDefinitions = useCallback(async () => {
        return APIService.get(API_URL)
            .then((response) => setJobDefinitions(response.map((jobDefinition: any) => {
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
                    },
                };
            })))
            .catch(displayError);
    }, [displayError]);

    const createJobDefinition = useCallback(
        async (newJobDefinition: JobDefinition) => {
            return APIService.post(API_URL, jobDefinitionToPayload(newJobDefinition))
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess(
                        `Successfully created job definition ${newJobDefinition.applicationName}`
                    );
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess]
    );

    const deleteJobDefinitions = useCallback(
        async (jobDefinitionsIds: number[]) => {
            return await Promise.all(
                jobDefinitionsIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess(
                        `Successfully deleted job definition${jobDefinitionsIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess]
    );

    const updateJobDefinition = useCallback(
        async (jobDefinition: JobDefinition) => {
            return APIService.put(`${API_URL}/${jobDefinition.id}`, jobDefinitionToPayload(jobDefinition))
                .then(() => {
                    fetchJobDefinitions();
                    displaySuccess("Successfully saved job definition");
                })
                .catch(displayError);
        },
        [fetchJobDefinitions, displayError, displaySuccess]
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

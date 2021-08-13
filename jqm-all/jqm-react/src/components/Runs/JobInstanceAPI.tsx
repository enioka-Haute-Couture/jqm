import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { JobInstance } from "./JobInstance";

const API_URL = "/client/ji";

export const useJobInstanceAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const [jobInstances, setQueues] = useState<JobInstance[] | null>();

    const fetchJobInstances = useCallback(async () => {
        return APIService.get(API_URL)
            .then((queues) => setQueues(queues))
            .catch(displayError);
    }, [displayError]);

    const createJobInstance = useCallback(
        async (newJobInstance: JobInstance) => {
            return APIService.post(API_URL, newJobInstance)
                .then(() => {
                    fetchJobInstances();
                    displaySuccess(`Successfully created job`);
                })
                .catch(displayError);
        },
        [fetchJobInstances, displayError, displaySuccess]
    );

    return {
        jobInstances,
        fetchJobInstances,
        createJobInstance,
    };
};

export default useJobInstanceAPI;

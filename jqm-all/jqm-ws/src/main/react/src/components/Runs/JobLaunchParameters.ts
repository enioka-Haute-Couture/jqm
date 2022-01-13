import { JobInstanceParameters } from "./JobInstance";

export interface JobLaunchParameters {
    applicationName: string;
    module: string;
    parameters: Array<JobInstanceParameters>;
    priority?: number;
    sessionID: string;
    startState: string;
    user: string;
}

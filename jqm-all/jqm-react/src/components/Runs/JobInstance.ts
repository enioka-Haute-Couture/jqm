export interface JobInstanceParameters {
    key: string;
    value: string;
}

export interface JobInstance {
    id?: number;
    application?: string;
    applicationName: string;
    beganRunningDate?: Date;
    definitionKeyword1: string;
    definitionKeyword2: string;
    definitionKeyword3: string;
    keyword1?: string;
    keyword2?: string;
    keyword3?: string;
    endDate?: Date;
    enqueueDate?: Date;
    fromSchedule: boolean;
    highlander: boolean;
    messages: Array<string>;
    nodeName: string;
    parameters: Array<JobInstanceParameters>;
    parent?: number;
    position: number;
    progress: number;
    queue: {
        id: number;
        name: string;
    };
    queueName: string;
    state: string;
    user: string;
    sessionID?: string;
    email?: string;
    module?: string;
    runAfter?: Date;
    priority?: number;
}

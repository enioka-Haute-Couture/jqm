export interface JobInstance {
    id?: number;
    applicationName: string;
    beganRunningDate?: Date;
    definitionKeyword1: string;
    definitionKeyword2: string;
    definitionKeyword3: string;
    endDate?: Date;
    enqueueDate?: Date;
    fromSchedule: boolean;
    highlander: boolean
    messages: Array<string>; // TODO:
    nodeName: string;
    parameters: Array<{
        key: string;
        value: string;
    }>
    parent?: number;
    position: number;
    progress: number;
    queue: {
        id: number;
        name: string;
    }
    queueName: string;
    state: string;
    user: string;
}

export interface Node {
    id?: number;
    port: number;
    lastSeenAlive: Date;
    name: string;
    tmpDirectory: string;
    dns: string;
    jmxRegistryPort: number;
    jmxServerPort: number;
    jobRepoDirectory: string;
    outputDirectory: string;
    rootLogLevel: string;
    loadApiAdmin: boolean;
    loadApiClient: boolean;
    loapApiSimple: boolean;
    stop: boolean;
    enabled: boolean;
}

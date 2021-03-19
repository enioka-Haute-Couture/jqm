export interface Node {
    id: number | null;
    name: string;
    dns: string;
    port: number;
    outputDirectory: string;
    jobRepoDirectory: string;
    rootLogLevel: string;
    lastSeenAlive: Date;
    jmxRegistryPort: number;
    jmxServerPort: number;
    tmpDirectory: string;
    loadApiAdmin: boolean;
    loadApiClient: boolean;
    loapApiSimple: boolean;
    stop: boolean | null;
    enabled: boolean | null;
}

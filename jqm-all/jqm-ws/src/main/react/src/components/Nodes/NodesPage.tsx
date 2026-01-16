import React, { useCallback, useEffect, useRef, useState } from "react";
import { Box, Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import DescriptionIcon from "@mui/icons-material/Description";
import { useTranslation } from "react-i18next";
import { useMUIDataTableTextLabels } from "../../utils/useMUIDataTableTextLabels";
import useNodesApi from "./NodesApi";
import { DisplayLogsDialog } from "./DisplayLogsDialog";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { HelpDialog } from "../HelpDialog";
import { setPageTitle } from "../../utils/title";

export const NodesPage: React.FC = () => {
    const { t } = useTranslation();
    const muiTableTextLabels = useMUIDataTableTextLabels(t("nodes.noMatch"));
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const nameInputRef = useRef(null);
    const dnsInputRef = useRef(null);
    const portInputRef = useRef(null);
    const outputDirInputRef = useRef(null);
    const repoDirInputRef = useRef(null);
    const tmpDirInputRef = useRef(null);
    const logLevelInputRef = useRef(null);
    const registryPortInputRef = useRef(null);
    const serverPortInputRef = useRef(null);
    const [loadApiAdmin, setLoadApiAdmin] = useState<boolean | null>(null);
    const [loadApiClient, setLoadApiClient] = useState<boolean | null>(null);
    const [loapApiSimple, setLoapApiSimple] = useState<boolean | null>(null);
    const [enabled, setEnabled] = useState<boolean | null>(null);

    const { canUserAccess } = useAuth();

    const { nodes, nodeLogs, setNodeLogs, fetchNodes, updateNode, fetchNodeLogs } =
        useNodesApi();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.node, PermissionAction.read)) {
            fetchNodes();
        }
        setPageTitle(t("nodes.title"));
        // eslint-disable-next-line
    }, [canUserAccess, t]);

    const handleOnViewLogs = useCallback(
        async (tableMeta: MUIDataTableMeta) => {
            fetchNodeLogs(tableMeta.rowData[3]);
        },
        [fetchNodeLogs]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [nodeId, stop, lastSeenAlive] = tableMeta.rowData;
            const { value: name } = nameInputRef.current!;
            const { value: dns } = dnsInputRef.current!;
            const { value: port } = portInputRef.current!;
            const { value: outputDirectory } = outputDirInputRef.current!;
            const { value: jobRepoDirectory } = repoDirInputRef.current!;
            const { value: tmpDirectory } = tmpDirInputRef.current!;
            const { value: rootLogLevel } = logLevelInputRef.current!;
            const { value: jmxRegistryPort } = registryPortInputRef.current!;
            const { value: jmxServerPort } = serverPortInputRef.current!;

            if (nodeId && name) {
                updateNode({
                    id: nodeId,
                    name: name,
                    dns: dns,
                    port: port,
                    outputDirectory: outputDirectory,
                    jobRepoDirectory: jobRepoDirectory,
                    rootLogLevel: rootLogLevel,
                    lastSeenAlive: lastSeenAlive,
                    jmxRegistryPort: jmxRegistryPort,
                    jmxServerPort: jmxServerPort,
                    tmpDirectory: tmpDirectory,
                    loadApiAdmin: loadApiAdmin!!,
                    loadApiClient: loadApiClient!!,
                    loapApiSimple: loapApiSimple!!,
                    stop: stop,
                    enabled: enabled,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateNode, loadApiAdmin, loadApiClient, loapApiSimple, enabled]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEnabled(tableMeta.rowData[12]);
        setLoapApiSimple(tableMeta.rowData[13]);
        setLoadApiClient(tableMeta.rowData[14]);
        setLoadApiAdmin(tableMeta.rowData[15]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

    const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "stop",
            label: "stop",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "lastSeenAlive",
            label: "lastSeenAlive",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "name",
            label: t("nodes.name"),
            options: {
                hint: t("nodes.hints.name"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    nameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "dns",
            label: t("nodes.dns"),
            options: {
                hint: t("nodes.hints.dns"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    dnsInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "port",
            label: t("nodes.httpPort"),
            options: {
                hint: t("nodes.hints.httpPort"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    portInputRef,
                    editingRowId,
                    false
                ),
            },
        },
        {
            name: "outputDirectory",
            label: t("nodes.outputDirectory"),
            options: {
                hint: t("nodes.hints.outputDirectory"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    outputDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jobRepoDirectory",
            label: t("nodes.jobRepoDirectory"),
            options: {
                hint: t("nodes.hints.jobRepoDirectory"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    repoDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "tmpDirectory",
            label: t("nodes.tmpDirectory"),
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    tmpDirInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "rootLogLevel",
            label: t("nodes.rootLogLevel"),
            options: {
                hint: t("nodes.hints.rootLogLevel"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    logLevelInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "jmxRegistryPort",
            label: t("nodes.jmxRegistryPort"),
            options: {
                hint: t("nodes.hints.jmxRegistryPort"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    registryPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "jmxServerPort",
            label: t("nodes.jmxServerPort"),
            options: {
                hint: t("nodes.hints.jmxServerPort"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    serverPortInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "enabled",
            label: t("nodes.enabled"),
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    enabled,
                    setEnabled
                ),
            },
        },
        {
            name: "loapApiSimple",
            label: t("nodes.simpleApi"),
            options: {
                hint: t("nodes.hints.simpleApi"),
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loapApiSimple,
                    setLoapApiSimple
                ),
            },
        },
        {
            name: "loadApiClient",
            label: t("nodes.clientApi"),
            options: {
                hint: t("nodes.hints.clientApi"),
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiClient,
                    setLoadApiClient
                ),
            },
        },
        {
            name: "loadApiAdmin",
            label: t("nodes.adminApi"),
            options: {
                hint: t("nodes.hints.adminApi"),
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    loadApiAdmin,
                    setLoadApiAdmin
                ),
            },
        },
        {
            name: "",
            label: t("common.actions"),
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    null,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.node, PermissionAction.update),
                    canUserAccess(PermissionObjectType.node, PermissionAction.delete),
                    [
                        {
                            title: t("nodes.viewNodeLogs"),
                            addIcon: () => <DescriptionIcon />,
                            action: handleOnViewLogs,
                        },
                    ],
                    t
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: muiTableTextLabels,
        download: false,
        print: false,
        selectableRows: "none" as SelectableRows,
        customToolbar: () => {
            return <>
                <Tooltip title={t("common.refresh")}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchNodes()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={t("common.help")}>
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
    };

    if (!canUserAccess(PermissionObjectType.node, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    return nodes ? (
        <Container maxWidth={false}>
            <DisplayLogsDialog
                showDialog={nodeLogs !== undefined}
                closeDialog={() => setNodeLogs(undefined)}
                logs={nodeLogs}
            />
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title={t("nodes.documentation.title")}
                header={t("nodes.documentation.header")}
                descriptionParagraphs={[
                    t("nodes.documentation.paragraph1"),
                    <>{t("nodes.documentation.paragraph2")} <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>{t("nodes.documentation.paragraph2Command")}</Box>{t("nodes.documentation.paragraph2End")}</>,
                    <>{t("nodes.documentation.paragraph3")} <Box component="span" sx={{ fontFamily: 'Monospace', fontWeight: 'bold' }}>{t("nodes.documentation.paragraph3Marker")}</Box> {t("nodes.documentation.paragraph3End")}</>
                ]}
            />
            <MUIDataTable
                title={t("nodes.title")}
                data={nodes}
                columns={columns}
                options={options}
            />
        </Container >
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

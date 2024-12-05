import React, { useCallback, useEffect, useRef, useState } from "react";
import { Badge, Box, Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import SettingsIcon from "@mui/icons-material/Settings";
import { EditParametersDialog } from "./EditParametersDialog";
import useJndiApi from "./JndiApi";
import { JndiResource } from "./JndiResource";
import { ResourceDropDownMenu } from "./ResourceDropDownMenu";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { HelpDialog } from "../HelpDialog";

export const JndiPage: React.FC = () => {
    const [showDropDown, setShowDropDown] = useState(false);
    const [showParameters, setShowParameters] = useState(false);
    const dropDownMenuPositionRef = useRef(null);
    const [currentSelectedResource, setCurrentSelectedResource] =
        useState<JndiResource | null>(null);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const selectedNameRef = useRef(null);
    const selectedTypeRef = useRef(null);
    const selectedFactoryRef = useRef(null);
    const selectedDescriptionRef = useRef(null);
    const [isSingleton, setIsSingleton] = useState<boolean>(false);

    const { canUserAccess } = useAuth();

    const { resources, fetchResources, saveResource, deleteResource } =
        useJndiApi();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.jndi, PermissionAction.read)) {
            fetchResources();
        }
    }, [fetchResources, canUserAccess]);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [paramId] = tableMeta.rowData;
            deleteResource([paramId]);
        },
        [deleteResource]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [resourceId, auth, parameters] = tableMeta.rowData;
            const { value: name } = selectedNameRef.current!;
            const { value: type } = selectedTypeRef.current!;
            const { value: factory } = selectedFactoryRef.current!;
            const { value: description } = selectedDescriptionRef.current!;

            const updatedParams =
                currentSelectedResource && currentSelectedResource.parameters
                    ? currentSelectedResource.parameters
                    : parameters;

            if (name && type && factory) {
                saveResource({
                    id: resourceId,
                    name: name,
                    type: type,
                    auth: auth,
                    description: description,
                    factory: factory,
                    singleton: isSingleton,
                    parameters: updatedParams,
                }).then(() => setEditingRowId(null));
            }
            setCurrentSelectedResource(null);
        },
        [saveResource, isSingleton, currentSelectedResource]
    );

    const handleOnCancel = useCallback(() => {
        setEditingRowId(null);
        setCurrentSelectedResource(null);
    }, []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setIsSingleton(tableMeta.rowData[6]);
        setEditingRowId(tableMeta.rowIndex);
        setCurrentSelectedResource({
            auth: tableMeta.rowData[1],
            name: tableMeta.rowData[2],
            type: tableMeta.rowData[3],
            factory: tableMeta.rowData[4],
            description: tableMeta.rowData[5],
            singleton: tableMeta.rowData[6],
            parameters: tableMeta.rowData[7],
        });
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
            name: "auth",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "name",
            label: "JNDI Alias",
            options: {
                hint: "The name that will be used by payloads during resource lookup.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedNameRef,
                    editingRowId
                ),
            },
        },
        {
            name: "type",
            label: "Type",
            options: {
                hint: "The fully qualified name of the resource class. E.g. java.io.File.File",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedTypeRef,
                    editingRowId
                ),
            },
        },
        {
            name: "factory",
            label: "Factory",
            options: {
                hint: "The fully qualified name of the resource factory class. E.g. com.enioka.jqm.providers.FileFactory",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedFactoryRef,
                    editingRowId
                ),
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                hint: "Free text to remember what the resource is for.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedDescriptionRef,
                    editingRowId
                ),
            },
        },
        {
            name: "singleton",
            label: "Singleton",
            options: {
                hint: "Tick for singleton resource. Differing from standard JNDI resource, a lookup on a singleton resource will always return the same instance (standard JNDI is: a new instance is created on each lookup). This is most helpful for connection pools, for which it is stupid to create a new instance on each call..",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    isSingleton,
                    setIsSingleton
                ),
            },
        },
        {
            name: "parameters",
            label: "Parameters",
            options: {
                filter: true,
                sort: false,
                customBodyRender: (value: any, tableMeta: MUIDataTableMeta) => {
                    const parameters = tableMeta.rowData[7];
                    const getBadge = (count: number) => (
                        <Badge badgeContent={count} color="primary" showZero>
                            <SettingsIcon />
                        </Badge>
                    );

                    return editingRowId === tableMeta.rowIndex ? (
                        <Tooltip
                            title={
                                editingRowId === tableMeta.rowIndex
                                    ? "Click to edit parameters"
                                    : ""
                            }
                        >
                            <span
                                style={{ cursor: "pointer" }}
                                onClick={() => setShowParameters(true)}
                            >
                                {getBadge(
                                    currentSelectedResource
                                        ? currentSelectedResource.parameters
                                            .length
                                        : 0
                                )}
                            </span>
                        </Tooltip>
                    ) : (
                        getBadge(parameters ? parameters.length : 0)
                    );
                },
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.jndi, PermissionAction.update),
                    canUserAccess(PermissionObjectType.jndi, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No JNDI resources found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.jndi, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.jndi, PermissionAction.create) &&
                    <>
                        <Tooltip title={"Create new JNDI resource"}>
                            <IconButton
                                color="default"
                                aria-label={"Create new JNDI resource"}
                                onClick={() => setShowDropDown(true)}
                                size="large">
                                <AddCircleIcon
                                    ref={dropDownMenuPositionRef}
                                />
                            </IconButton>
                        </Tooltip>
                        <ResourceDropDownMenu
                            menuPositiontRef={dropDownMenuPositionRef}
                            show={showDropDown}
                            handleSet={setShowDropDown}
                            onOpen={() => setShowDropDown(true)}
                            onClose={() => setShowDropDown(false)}
                            onSelectResource={(newResource: JndiResource) => {
                                delete newResource.uiName;
                                saveResource({ ...newResource, name: `${newResource.name}_${new Date().getTime()}` });
                                setShowDropDown(false);
                            }}
                        />
                    </>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchResources()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Help"}>
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const paramIds: number[] = [];
            data.forEach(({ index }) => {
                const resource = resources ? resources[index] : null;
                if (resource) {
                    paramIds.push(resource.id!);
                }
            });
            deleteResource(paramIds);
        },
    };

    if (!canUserAccess(PermissionObjectType.jndi, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    return resources ? (
        <Container maxWidth={false}>
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title="JNDI Resources documentation"
                header="Resources are Java objects which can be requested by jobs through a JNDI lookup."
                descriptionParagraphs={[
                    <>On this page, one may create or change resources. Creating a new resource does not require any reboot. Altering an already used <Box component="span" sx={{ fontWeight: 'bold' }}>singleton</Box> resource does require rebooting the nodes using it, as singleton resources are cached. Altering a non-singleton resource does not require any reboot.</>,
                    "Please note that for a non-singleton resource, its class and factory class must be accessible to the payload: either in JQM_ROOT/ext or inside the payload's own dependencies. For a singleton resource, the classes must be inside ext (as they must be available to the engine itself, not only to the payload)."
                ]}
            />
            <MUIDataTable
                title={"JNDI Resources"}
                data={resources}
                columns={columns}
                options={options}
            />
            {showParameters &&
                <EditParametersDialog
                    showDialog={true}
                    selectedResource={currentSelectedResource}
                    setSelectedResource={(resource: JndiResource) =>
                        setCurrentSelectedResource(resource)
                    }
                    closeDialog={() => setShowParameters(false)}
                />
            }
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

import React, { useState, useCallback, useRef, useEffect } from "react";
import { Container, Grid, IconButton, Tooltip, Badge } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable, { Display } from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SettingsIcon from "@material-ui/icons/Settings";
import {
    renderInputCell,
    renderBooleanCell,
    renderActionsCell,
} from "../TableCells";
import { EditParametersDialog } from "./EditParametersDialog";
import useJndiApi from "./useJndiApi";
import { JndiResource } from "./JndiResource";
import { ResourceDropDownMenu } from "./ResourceDropDownMenu";
import { PermissionObjectType, PermissionAction, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";

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
        (tableMeta) => {
            const [paramId] = tableMeta.rowData;
            deleteResource([paramId]);
        },
        [deleteResource]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
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

    const handleOnEdit = useCallback((tableMeta) => {
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
                customBodyRender: (value: any, tableMeta: any) => {
                    const parameters = tableMeta.rowData[7];
                    const getBadge = (count: number) => (
                        <Badge badgeContent={count} color="primary">
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
                filter: true,
                sort: true,
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
        download: false,
        print: false,
        customToolbar: () => {
            return (
                <>
                    {canUserAccess(PermissionObjectType.jndi, PermissionAction.create) &&
                        <>
                            <Tooltip title={"Create new JNDI resource"}>
                                <IconButton
                                    color="default"
                                    aria-label={"Create new JNDI resource"}
                                    onClick={() => setShowDropDown(true)}
                                >
                                    <AddCircleIcon
                                        innerRef={dropDownMenuPositionRef}
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
                                    saveResource(newResource);
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
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
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
            <MUIDataTable
                title={"JNDI Resources"}
                data={resources}
                columns={columns}
                options={options}
            />
            <EditParametersDialog
                showDialog={showParameters}
                selectedResource={currentSelectedResource}
                setSelectedResource={(resource: JndiResource) =>
                    setCurrentSelectedResource(resource)
                }
                closeDialog={() => setShowParameters(false)}
            />
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

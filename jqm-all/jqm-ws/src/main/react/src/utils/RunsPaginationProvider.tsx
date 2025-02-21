import React, { createContext, ReactNode, useContext, useState } from "react";
import { MUISortOptions } from "mui-datatables";

const defaultState: RunsPaginationState = {
    page: 0,
    rowsPerPage: 10,
    sortOrder: {
        name: "id",
        direction: "desc",
    },
    filterList: [],
    queryLiveInstances: false
}

interface RunsPaginationState {
    page: number;
    rowsPerPage: number;
    sortOrder: MUISortOptions;
    filterList: string[][];
    queryLiveInstances: boolean;
}

interface RunsPagination extends RunsPaginationState {
    setPage: (page: number) => void;
    setRowsPerPage: (rowsPerPage: number) => void;
    setSortOrder: (sortOrder: MUISortOptions) => void;
    setFilterList: (filterList: string[][]) => void;
    setQueryLiveInstances: (queryLiveInstances: boolean) => void;
}

const RunsPaginationContext = createContext<RunsPagination>({
    ...defaultState,
    setPage: () => { },
    setRowsPerPage: () => { },
    setSortOrder: () => { },
    setFilterList: () => { },
    setQueryLiveInstances: () => { },
});
export const useRunsPagination = () => useContext(RunsPaginationContext);

export const RunsPaginationProvider = ({ children }: { children: ReactNode }) => {
    const [rowsPerPage, setRowsPerPage] = useState<number>(defaultState.rowsPerPage);
    const [page, setPage] = useState<number>(defaultState.page);
    const [sortOrder, setSortOrder] = useState<MUISortOptions>(
        defaultState.sortOrder);
    const [filterList, setFilterList] = useState<string[][]>(
        defaultState.filterList);
    const [queryLiveInstances, setQueryLiveInstances] =
        useState<boolean>(false);

    return (
        <RunsPaginationContext.Provider
            value={{
                page,
                rowsPerPage,
                sortOrder,
                filterList,
                queryLiveInstances,
                setPage,
                setRowsPerPage,
                setSortOrder,
                setFilterList,
                setQueryLiveInstances
            }}
        >
            {children}
        </RunsPaginationContext.Provider>
    );
};

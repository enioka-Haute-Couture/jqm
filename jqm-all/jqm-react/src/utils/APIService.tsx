const API_URL = "/api";

export default class APIService {
    static headers() {
        return {
            Accept: "application/json",
        };
    }

    static get(
        url: string,
        { headers = {} } = {},
    ) {
        return this.request(url, {
            method: "GET",
            headers: {
                ...this.headers(),
                ...headers,
            },
        });
    }

    static delete(url: string, { headers = {} } = {}) {
        return this.request(url, {
            method: "DELETE",
            headers: {
                ...headers,
            },
        });
    }

    static post(
        url: string,
        content: any,
        { headers = {}, formEncoded = false } = {}
    ) {
        return this.request(url, {
            method: "POST",
            headers: {
                ...(formEncoded ? {} : { "Content-Type": "application/json" }),
                ...this.headers(),
                ...headers,
            },
            body: formEncoded
                ? new URLSearchParams(content)
                : JSON.stringify(content),
        });
    }

    static put(url: string, content: any, { headers = {} } = {}) {
        return this.request(url, {
            method: "PUT",
            headers: {
                ...{ "Content-Type": "application/json" },
                ...this.headers(),
                ...headers,
            },
            body: JSON.stringify(content),
        });
    }

    static async request(url: string, init: any) {
        var res = await fetch(API_URL + url, {
            credentials: "same-origin",
            ...init,
        });

        if (res.ok) {
            if (res.status === 200 || res.status === 201) {
                return await res.json();
            }
            return init.method !== "GET";
        }

        let error: any;
        try {
            error = { code: res.status, details: await res.json() };
        } catch {
            error = { code: res.status, message: res.statusText };
        }
        throw error;
    }

    static getUrl() {
        return API_URL;
    }
}

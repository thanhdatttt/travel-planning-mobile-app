interface Response<T = unknown> {
    message?: string;
    data?: T;
    error?: string | string[];
}

export const createResponse = <T>(params: Response<T>) => {
    return {
        message: params.message ?? null,
        data: params.data,
        error: params.error,
    };
}
interface Metadata {
  page?: number;
  limit?: number;
  total?: number;
  lastPage?: number;
  [key: string]: any; //Allows for flexible extra fields if needed
}

interface Response<T = unknown> {
    message?: string;
    data?: T;
    metadata?: Metadata;
    error?: string | string[];
}

export const createResponse = <T>(params: Response<T>) => {
    return {
        message: params.message ?? null,
        data: params.data,
        metadata: params.metadata,
        error: params.error,
    };
}
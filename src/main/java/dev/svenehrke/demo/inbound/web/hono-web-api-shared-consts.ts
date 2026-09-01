// Java-HONO: hand-kept in sync with HonoWebApiSharedConsts.java (the two mutation
// endpoint path templates — see PersonActionResource.java). Can't be generated
// like JTSPersonRouteName / JTSPersonEventName because the TS side consumes the
// actual runtime string values, not just the type.
export const HonoWebApiConsts = {
	PERSON: `/person/{id}`,
	DELETE: `/delete`,
};

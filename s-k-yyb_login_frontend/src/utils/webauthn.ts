export const base64urlToBuffer = (base64urlString: string): Uint8Array => {
    

    const padding = '='.repeat((4 - (base64urlString.length % 4)) % 4);
    const base64 = (base64urlString + padding)
      .replace(/-/g, '+')
      .replace(/_/g, '/');
    const raw = atob(base64);
    return Uint8Array.from([...raw].map(char => char.charCodeAt(0)));
  };

export const preformatMakeCredReq = (makeCredReq: any) => {
    makeCredReq.challenge = base64urlToBuffer(makeCredReq.challenge);
    makeCredReq.user.id = base64urlToBuffer(makeCredReq.user.id);

    // ✅ 문제 해결 핵심: 확장 확실히 제거
    delete makeCredReq.extensions;

    return makeCredReq;
};
  
export const bufferToBase64url = (buffer: ArrayBuffer | Uint8Array): string => {
    
    const bytes = buffer instanceof Uint8Array ? buffer : new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  };

  export const publicKeyCredentialToJSON = (cred: any): any => {
    if (cred instanceof ArrayBuffer) {
      return bufferToBase64url(cred);
    }
  
    if (cred instanceof Uint8Array) {
      return bufferToBase64url(cred.buffer);
    }
  
    if (Array.isArray(cred)) {
      return cred.map(publicKeyCredentialToJSON);
    }
  
    if (cred && typeof cred === 'object') {
      const obj: any = {};
      for (const key in cred) {
        const val = cred[key];
        if (typeof val !== 'function') {
          obj[key] = publicKeyCredentialToJSON(val);
        }
      }
  
      // ✅ 핵심: clientExtensionResults가 없으면 빈 객체 추가
      if ('response' in obj && !obj.clientExtensionResults) {
        obj.clientExtensionResults = {};
      }
  
      return obj;
    }
  
    return cred;
  };



export const makeCredentialRequestToJSON = (req: any): any => {
    const copy: any = { ...req };

    // challenge → base64url 문자열로
    if (copy.challenge instanceof Uint8Array) {
        copy.challenge = bufferToBase64url(copy.challenge);
    }

    // user.id → base64url 문자열로
    if (copy.user?.id instanceof Uint8Array) {
        copy.user.id = bufferToBase64url(copy.user.id);
    }

    return copy;
};
  


export const preformatGetAssertReq = (assertionReq: any) => {
    return assertionReq; // 가공 없이 그대로 넘김
  };
  
export const preformatAssertionRequest = (assertionReq: any) => {
  assertionReq.challenge = base64urlToBuffer(assertionReq.challenge);

  if (assertionReq.allowCredentials) {
    assertionReq.allowCredentials = assertionReq.allowCredentials.map((cred: any) => {
      const formatted = {
        ...cred,
        id: base64urlToBuffer(cred.id),
      };
      if (!Array.isArray(cred.transports)) {
        delete formatted.transports;
      }
      return formatted;
    });
  }

  if (assertionReq.extensions) {
    for (const key in assertionReq.extensions) {
      if (assertionReq.extensions[key] === null) {
        delete assertionReq.extensions[key];
      }
    }
    if (Object.keys(assertionReq.extensions).length === 0) {
      delete assertionReq.extensions;
    }
  }

  return assertionReq;
};



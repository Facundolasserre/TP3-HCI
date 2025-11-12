# 🐛 REPORTE DE BUG: Placeholder <%TEMPORARY_PASSWORD%> no se reemplaza en email de recuperación

## 📋 Descripción del Problema

Cuando un usuario usa la opción "Forgot Password", recibe un email pero el placeholder `<%TEMPORARY_PASSWORD%>` no se reemplaza con el código real. El email llega con el texto literal:

```
Password recovery
Your temporary password has been generated
Your temporary password is <%TEMPORARY_PASSWORD%>
Please change it after logging in
```

**Esperado:** El código generado debería reemplazar `<%TEMPORARY_PASSWORD%>`

---

## 🔍 Análisis del Problema

### Archivo 1: Plantilla del email
**Ubicación:** `/api/templates/reset-password.mft`

```html
<div style="text-align: center;">
    <h1>
        <strong>Password recovery</strong>
    </h1>
    <p>
        <span>Your temporary password has been generated</span>
    </p>
    <h3>
        <strong>Your temporary password is <span style="color: #fc987e;"><%TEMPORARY_PASSWORD%></span></strong>
    </h3>
    <p>
        <span>Please change it after logging in</span>
    </p>
</div>
```

✅ **Placeholder usado:** `<%TEMPORARY_PASSWORD%>`

---

### Archivo 2: Servicio de email
**Ubicación:** `/api/src/services/email.service.ts`

**Línea ~152-158:**
```typescript
private getResetPasswordEmailTemplate(token: string, expirationDate: Date): string {
    let template = readFileContent("templates/reset-password.mft");
    if (!template) template = DEFAULT_RESET_PASSWORD_TEMPLATE;

    return template
      .replace(/<%EXPIRATION_DATE%>/g, expirationDate.toLocaleString())
      .replace(/<%VERIFICATION_CODE%>/g, token);  // ← PROBLEMA AQUÍ
}
```

❌ **Placeholder que intenta reemplazar:** `<%VERIFICATION_CODE%>`

---

## 🎯 La Raíz del Problema

**Desincronización entre placeholder:**

| Archivo | Placeholder |
|---------|------------|
| `reset-password.mft` | `<%TEMPORARY_PASSWORD%>` ✅ |
| `email.service.ts` método | `<%VERIFICATION_CODE%>` ❌ |

El código intenta reemplazar `<%VERIFICATION_CODE%>` en la plantilla, pero la plantilla usa `<%TEMPORARY_PASSWORD%>`. Por eso el placeholder nunca se reemplaza.

---

## ✅ Solución

### Opción 1: Cambiar el código (Recomendado)
En `/api/src/services/email.service.ts`, línea ~157, cambiar:

```typescript
// CAMBIAR ESTO:
.replace(/<%VERIFICATION_CODE%>/g, token);

// POR ESTO:
.replace(/<%TEMPORARY_PASSWORD%>/g, token);
```

---

### Opción 2: Cambiar la plantilla
En `/api/templates/reset-password.mft`, cambiar:

```html
<!-- CAMBIAR ESTO: -->
Your temporary password is <span style="color: #fc987e;"><%TEMPORARY_PASSWORD%></span>

<!-- POR ESTO: -->
Your temporary password is <span style="color: #fc987e;"><%VERIFICATION_CODE%></span>
```

---

## 📝 Recomendación

**Usar Opción 1** (cambiar el código) porque:
- ✅ Semánticamente más correcto (es una "contraseña temporal", no un "código de verificación")
- ✅ La plantilla es más clara con `<%TEMPORARY_PASSWORD%>`
- ✅ El nombre `TEMPORARY_PASSWORD` describe mejor el contenido

---

## 🔐 Nota de Seguridad

El token que se envía es una contraseña temporal generada. El usuario debe cambiarla después de loguearse. No es un "código de verificación" en el sentido del registro (que es de 16 caracteres hexadecimales).

---

## 📊 Ubicaciones exactas a revisar

1. **Plantilla (confirmada correcta):**
   - Archivo: `/api/templates/reset-password.mft`
   - Placeholder: `<%TEMPORARY_PASSWORD%>` ✅

2. **Código (necesita reparación):**
   - Archivo: `/api/src/services/email.service.ts`
   - Método: `getResetPasswordEmailTemplate()`
   - Línea: ~157
   - Error: Intenta reemplazar `<%VERIFICATION_CODE%>` cuando debería ser `<%TEMPORARY_PASSWORD%>`

---

## 🎯 Resumen

**Problema:** Placeholder no se reemplaza
**Causa:** Desincronización entre nombre de placeholder en plantilla vs código
**Solución:** Cambiar `.replace(/<%VERIFICATION_CODE%>/g, token)` a `.replace(/<%TEMPORARY_PASSWORD%>/g, token)` en `email.service.ts`
**Archivos afectados:** 1 archivo (`email.service.ts`)
**Líneas a cambiar:** 1 línea (línea ~157)

✅ **Bug completamente identificado y documentado**


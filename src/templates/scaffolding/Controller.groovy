<%=packageName ? "package ${packageName}\n\n" : ''%>import org.springframework.dao.DataIntegrityViolationException
import grails.converters.JSON
import static javax.servlet.http.HttpServletResponse.*

class ${className}Controller {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() { }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        render ${className}.list(params) as JSON
    }

    def save() {
        def ${propertyName} = new ${className}(params)
        def responseJson = [:]
        if (${propertyName}.save(flush: true)) {
            responseJson.status = 'ok'
            responseJson.id = ${propertyName}.id
            responseJson.message = message(code: 'default.created.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])
        } else {
            responseJson.status = 'error'
            responseJson.errors = ${propertyName}.errors.fieldErrors.collectEntries {
                [(it.field): message(error: it)]
            }
        }
        render responseJson as JSON
    }

    def get() {
        def ${propertyName} = ${className}.get(params.id)
        if (${propertyName}) {
			render ${propertyName} as JSON
        } else {
			notFound params.id
		}
    }

    def update() {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            notFound params.id
            return
        }

        def responseJson = [:]

        if (params.version) {
            def version = params.long('version')
            if (${propertyName}.version > version) {<% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
                render status: SC_CONFLICT, text: message(code: 'default.optimistic.locking.failure',
                          args: [message(code: '${domainClass.propertyName}.label', default: '${className}')],
                          default: 'Another user has updated this ${className} while you were editing')
                return
            }
        }

        ${propertyName}.properties = params

        if (${propertyName}.save(flush: true)) {
            responseJson.status = 'ok'
            responseJson.id = ${propertyName}.id
            responseJson.message = message(code: 'default.updated.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])
        } else {
            responseJson.status = 'error'
            responseJson.errors = ${propertyName}.errors.fieldErrors.collectEntries {
                [(it.field): message(error: it)]
            }
        }

        render responseJson as JSON
    }

    def delete() {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            notFound params.id
            return
        }

        def responseJson = [:]
        try {
            ${propertyName}.delete(flush: true)
            responseJson.status = 'ok'
            responseJson.message = message(code: 'default.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])
        } catch (DataIntegrityViolationException e) {
            responseJson.status = 'error'
            responseJson.message = message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])
        }
        render responseJson as JSON
    }

    private void notFound(id) {
        response.sendError SC_NOT_FOUND, message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])
    }
}
package utils
import java.io.File

class TemplateCounter() {
    //paths to ETP.ReportServer files
    private val projectBasePath = System.getProperty("user.dir")!!
    private val templatesDirName = "/jetty-resources"
    private val templatesPropertiesPath = "/src/main/resources/templates.properties"
    private val jettyResourcesGuDirPath = "$projectBasePath/counter/src/inputProject/ETP.ReportServer/jetty-resources-gu"
    private val reportGuDirPath = "$projectBasePath/counter/src/inputProject/ETP.ReportServer/report-gu"
    private val reportXMLDirPath = "$projectBasePath/counter/src/inputProject/ETP.ReportServer/report-xml"
    private val reportHbaseDirPath = "$projectBasePath/counter/src/inputProject/ETP.ReportServer/report-hbase"
    private val jettyResourcesEpdDirPath = "$projectBasePath/counter/src/inputProject/ETP.ReportServer/jetty-resources-epd"

    //template elements names for counting (at jrxml)
    private val templateElementFieldName = "</field>"
    private val templateElementStaticTextName = "<staticText>"
    private val templateElementTextFieldName = "<textField"
    private val templateElementVariableName = "<variable "

    //maps (should be private)
    val templatesNamesMap : MutableMap<String, String> = mutableMapOf()
    val templatePropList : MutableList<String> = mutableListOf()
    val guTemplatesMap : MutableMap<String, List<String>> = mutableMapOf()

    init {
        initTemplateProperties()
        initFilesNames()
        initGuTamplates()
    }

    fun getSubReports(fileName : String):List<String>{
       // <subreportExpression><![CDATA["gu_mka_general_part_000056.jasper"]]></subreportExpression>
        val listOfReports : MutableList<String> = mutableListOf(fileName)
        val tmpList : MutableList<String> = mutableListOf(fileName)

        while (tmpList.isNotEmpty()){
            val list : MutableList<String> = mutableListOf()
            tmpList.forEach {
                if(templatesNamesMap.keys.contains(it)){
                    File(templatesNamesMap[it]!!).readLines().forEach{line ->
                        if(line.contains("<subreportExpression>")){
                            val reportName = line.substringAfter("CDATA[\"").substringBefore("\"]]").replace("jasper","jrxml")
                            listOfReports.add(reportName)
                            list.add(reportName)
                        }
                    }
                }
            }
            tmpList.clear()
            tmpList.addAll(list)
        }

        return listOfReports
    }

    fun calculateElementsCount(elementName:String):  Map<String,Int>{
      val resultMap: MutableMap<String, Int> = mutableMapOf()
        guTemplatesMap.forEach {
            var elementsCount = 0
            it.value.forEach {
                elementsCount+= File(templatesNamesMap["gu_064701.jrxml"]!!).readLines().count{ it.contains( elementName)}
            }
            resultMap[it.key] = elementsCount
        }
      return resultMap
    }

    private  fun initFilesNames(){
        //get all templates names as key and path to file as value from directories: report-gu, report-xml, jetty-resources-gu
        File(jettyResourcesGuDirPath).walkTopDown().forEach { if(it.name.contains("jrxml")) templatesNamesMap[it.name] = it.canonicalPath }
        File(jettyResourcesEpdDirPath).walkTopDown().forEach { if(it.name.contains("jrxml")) templatesNamesMap[it.name] = it.canonicalPath }
        File(reportGuDirPath+templatesDirName).walkTopDown().forEach { if(it.name.contains("jrxml")) templatesNamesMap[it.name] = it.canonicalPath }
        File(reportXMLDirPath+templatesDirName).walkTopDown().forEach { if(it.name.contains("jrxml")) templatesNamesMap[it.name] = it.canonicalPath }
        File(reportHbaseDirPath+templatesDirName).walkTopDown().forEach { if(it.name.contains("jrxml")) templatesNamesMap[it.name] = it.canonicalPath }
    }

    private fun initTemplateProperties(){
        //get templates properties from report-gu, report-xml
        File(reportGuDirPath+templatesPropertiesPath).readLines().forEach {if(!(it.replace(" ","").isEmpty() || it.contains("#"))) templatePropList.add(it) }
        File(reportXMLDirPath+templatesPropertiesPath).readLines().forEach {if(!(it.replace(" ","").isEmpty() || it.contains("#"))) templatePropList.add(it) }
        File(reportHbaseDirPath+templatesPropertiesPath).readLines().forEach {if(!(it.replace(" ","").isEmpty() || it.contains("#"))) templatePropList.add(it) }
    }

    private fun initGuTamplates(){
        //fill gu with templates that are has templates properties. Key is gu_{serviceCode}, value is template name
        templatePropList.forEach {
            guTemplatesMap[it.substringBefore(".jasperTemplate")] = getSubReports(it.substringAfter("=").replace("jasper","jrxml"))
        }
        //fill gu with templates that are not in templates properties. Key is gu_{serviceCode}, value is template name
        templatesNamesMap.forEach {
            val tmpName = it.key.substringBefore(".jrxml")
            if((tmpName.count{ chr -> "_".contains(chr)} < 2 || tmpName.contains("epd_dolg") || tmpName=="epd_nach"|| tmpName=="epd_nach_QR") &&  !guTemplatesMap.keys.contains(tmpName))  guTemplatesMap[it.key.substringBefore(".jrxml")] = getSubReports(it.key)
        }
    }
}
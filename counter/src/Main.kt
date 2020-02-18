import utils.TemplateCounter

fun main(){
/*
get main report name save to map 1, where key is main template name, value is path to template
get all tem plates.properties from paths  // concatenate all templates properties // save to map
build by template names and template.properties gu map, where key is gu name, value is list of reports for gu
calculate gu difficult : to 50 fields is easy, from 51 to 150 field is middle, more than 150 fields hard (count of TextFields elements)
*/
    val templateCounter = TemplateCounter()
        templateCounter.calculateElementsCount("<textField").forEach{
            println("gu name : "+ it.key+ " difficult: "+it.value+" count of templates:" + templateCounter.guTemplatesMap[it.key]!!.size+ " main template name: "+templateCounter.guTemplatesMap[it.key]!!.first() )
        }
}

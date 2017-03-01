/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.democonfiguration;

import com.biqasoft.gateway.objects.custom.template.CustomObjectsRepository;
import com.biqasoft.entity.constants.CUSTOM_FIELD_STYLE;
import com.biqasoft.entity.constants.CUSTOM_FIELD_TYPES;
import com.biqasoft.entity.core.objects.field.CustomDictionary;
import com.biqasoft.entity.core.objects.field.CustomDictionaryItem;
import com.biqasoft.entity.core.objects.CustomField;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 4/6/2015
 * All Rights Reserved
 */

@Service
public class CreateTestDataEsid {

    private final CustomObjectsRepository customObjectsRepository;

    @Autowired
    public CreateTestDataEsid(CustomObjectsRepository customObjectsRepository) {
        this.customObjectsRepository = customObjectsRepository;
    }

    /**
     * Единая система интеграции данных (ЕСИД)
     * Система электронногодокументооборота
     */
    public void createEsid() {
        CustomObjectTemplate customObjectTemplate = new CustomObjectTemplate();
        customObjectTemplate.setName("Договор");
        customObjectTemplate.setDescription("Документ, используемый для электронного документооборота");

        CustomField field = new CustomField();
        field.setName("Номер основного договора");
        field.setDescription("Номер договора");
        field.setType(CUSTOM_FIELD_TYPES.STRING);
        customObjectTemplate.getCustomFields().add(field);

        CustomField fieldDate = new CustomField();
        fieldDate.setType(CUSTOM_FIELD_TYPES.DATE);
        fieldDate.setName("Дата договора");
        fieldDate.setDescription("Дата договора");
        customObjectTemplate.getCustomFields().add(fieldDate);

        //////////////////////////////////////////////////////////////
        //////////////      Статус договора       ////////////////////
        //////////////////////////////////////////////////////////////
        CustomField fieldStatus = new CustomField();
        fieldStatus.setType(CUSTOM_FIELD_TYPES.DICTIONARY);
        fieldStatus.setName("Статус");
        fieldStatus.setDescription("Статус договора");

        CustomDictionary dictionary = new CustomDictionary();
        fieldStatus.getValue().setDictVal(dictionary);

        CustomDictionaryItem item0 = new CustomDictionaryItem("Создано");
        CustomDictionaryItem item1 = new CustomDictionaryItem("Согласование");
        CustomDictionaryItem item2 = new CustomDictionaryItem("Ожидание нашего подписания");
        CustomDictionaryItem item3 = new CustomDictionaryItem("Ожидание подписания контрагента");
        CustomDictionaryItem item4 = new CustomDictionaryItem("Заключен");
        CustomDictionaryItem item5 = new CustomDictionaryItem("Расторгнут");
        CustomDictionaryItem item6 = new CustomDictionaryItem("Вышел срок действия");

        dictionary.setValue(item0);
        dictionary.getValues().add(item0);
        dictionary.getValues().add(item1);
        dictionary.getValues().add(item2);
        dictionary.getValues().add(item3);
        dictionary.getValues().add(item4);
        dictionary.getValues().add(item5);
        dictionary.getValues().add(item6);

        customObjectTemplate.getCustomFields().add(fieldStatus);
        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////

        CustomField fieldSubject = new CustomField();
        fieldSubject.setType(CUSTOM_FIELD_TYPES.STRING);
        fieldSubject.setName("Предмет договора");
        fieldSubject.setDescription("Тема договора: закупка/продажа/внутренний договор и прочее");
        customObjectTemplate.getCustomFields().add(fieldSubject);

        CustomField fieldForeign = new CustomField();
        fieldForeign.setType(CUSTOM_FIELD_TYPES.BOOLEAN);
        fieldForeign.setName("Зарубежный контрагент");
        fieldForeign.setDescription("Контрагент - иностранное лицо");
        customObjectTemplate.getCustomFields().add(fieldForeign);

        CustomField fieldBuyType = new CustomField();
        fieldBuyType.setType(CUSTOM_FIELD_TYPES.STRING);
        fieldBuyType.setName("Способ закупки");
        customObjectTemplate.getCustomFields().add(fieldBuyType);

        CustomField fieldComment = new CustomField();
        fieldComment.setType(CUSTOM_FIELD_TYPES.STRING);
        fieldComment.setStyle(CUSTOM_FIELD_STYLE.STRING_RICH_TEXT);
        fieldComment.setName("Комментарии");
        customObjectTemplate.getCustomFields().add(fieldComment);

        CustomField fieldExpired = new CustomField();
        fieldExpired.setType(CUSTOM_FIELD_TYPES.DATE);
        fieldExpired.setName("Дата окночания");
        fieldExpired.setDescription("Дата окончания действия договора");
        customObjectTemplate.getCustomFields().add(fieldExpired);

        CustomField fieldWithoutNDS = new CustomField();
        fieldWithoutNDS.setName("Сумма без НДС");
        fieldWithoutNDS.setType(CUSTOM_FIELD_TYPES.INTEGER);
        customObjectTemplate.getCustomFields().add(fieldWithoutNDS);

        CustomField fieldWithNDS = new CustomField();
        fieldWithNDS.setName("Сумма с НДС");
        fieldWithNDS.setType(CUSTOM_FIELD_TYPES.INTEGER);
        customObjectTemplate.getCustomFields().add(fieldWithNDS);

        CustomField fieldAttach = new CustomField();
        fieldAttach.setName("Скан документа");
        fieldAttach.setDescription("Скан документа, электронная копия, фотография где есть полный документ с подписями всех сторон");
        fieldAttach.setType(CUSTOM_FIELD_TYPES.DOCUMENT_FILE);
        customObjectTemplate.getCustomFields().add(fieldAttach);

        customObjectsRepository.addCustomObject(customObjectTemplate);

    }


}

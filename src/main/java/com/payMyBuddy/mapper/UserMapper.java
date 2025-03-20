package com.payMyBuddy.mapper;

import com.payMyBuddy.dto.user.ContactResponseDTO;
import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.dto.user.UserUpdateDTO;
import com.payMyBuddy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface User mapper.
 */
@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface UserMapper {

    /**
     * From entity user to user response dto.
     *
     * @param user the user
     * @return the user response dto
     */
    @Mapping(target = "accounts", source = "accounts")
    @Mapping(target = "contacts", source = "contacts")
    UserResponseDTO toUserResponseDTO(User user);

    /**
     * From entity contact to contact response dto.
     *
     * @param contact the contact
     * @return the contact response dto
     */
    @Mapping(target = "contactId", source = "id")
    ContactResponseDTO toContactResponseDTO(User contact);

    /**
     * From update dto user to entity user.
     *
     * @param userUpdateDTO the user update dto
     * @return the user
     */
    User toEntityFromUpdateDTO(UserUpdateDTO userUpdateDTO);

    /**
     * From create dto user to entity user.
     *
     * @param userCreateDTO the user create dto
     * @return the user
     */
    User toEntityFromCreateDTO(UserCreateDTO userCreateDTO);
}
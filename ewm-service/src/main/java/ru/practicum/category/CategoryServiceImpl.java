package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.InvalidEventStateOrDate;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto category) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(category)));
    }

    @Override
    public CategoryDto updateCategory(int categoryId, CategoryDto categoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ObjectNotFoundException(categoryId,
                                "Category with id " + categoryId + " was not found")),
                categoryDto)));
    }

    @Override
    public void deleteCategory(int categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException(categoryId,
                        "Category with id " + categoryId + " was not found"));

        if (!eventRepository.findByCategoryId(categoryId).isEmpty()) {
            throw new InvalidEventStateOrDate("The category is not empty");
        }

        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        final Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return CategoryMapper.toCategoryDto(categoryRepository.findAll(page));
    }

    @Override
    public CategoryDto getCategoryById(int categoryId) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException(categoryId,
                        "Category with id " + categoryId + " was not found")));
    }
}
